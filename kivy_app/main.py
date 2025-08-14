import json
import os
import threading
from datetime import datetime
from typing import Any, Dict, Optional

from kivy.app import App
from kivy.clock import Clock
from kivy.lang import Builder
from kivy.metrics import dp
from kivy.properties import ObjectProperty, StringProperty, NumericProperty, BooleanProperty
from kivy.uix.screenmanager import ScreenManager, Screen, NoTransition


class FirebaseService:
    """Thin wrapper around Pyrebase to isolate Firebase operations."""

    def __init__(self, config_path: str) -> None:
        self._pyrebase = None
        self._auth = None
        self._db = None
        self._user: Optional[Dict[str, Any]] = None

        if not os.path.exists(config_path):
            raise FileNotFoundError(f"Missing Firebase config at: {config_path}")

        with open(config_path, "r", encoding="utf-8") as fh:
            config = json.load(fh)

        try:
            import pyrebase
            self._pyrebase = pyrebase.initialize_app(config)
            self._auth = self._pyrebase.auth()
            self._db = self._pyrebase.database()
        except Exception as exc:  # noqa: BLE001
            print(f"[FirebaseService] Failed to initialize Pyrebase: {exc}")

    # Authentication
    def create_user(self, email: str, password: str) -> Dict[str, Any]:
        return self._auth.create_user_with_email_and_password(email, password)

    def sign_in(self, email: str, password: str) -> Dict[str, Any]:
        self._user = self._auth.sign_in_with_email_and_password(email, password)
        return self._user

    def get_current_user_id(self) -> Optional[str]:
        if self._user and "localId" in self._user:
            return self._user["localId"]
        return None

    # Database helpers
    def set_user_profile(self, user_id: str, profile: Dict[str, Any]) -> None:
        self._db.child("Users").child(user_id).set(profile)

    def update_user_fields(self, user_id: str, fields: Dict[str, Any]) -> None:
        self._db.child("Users").child(user_id).update(fields)

    def read_user(self, user_id: str) -> Optional[Dict[str, Any]]:
        snap = self._db.child("Users").child(user_id).get()
        return snap.val() if snap and snap.val() else None

    def get_user_field(self, user_id: str, field: str) -> Any:
        snap = self._db.child("users").child(user_id).child(field).get()
        return snap.val() if snap else None

    def set_user_field(self, user_id: str, field: str, value: Any) -> None:
        self._db.child("users").child(user_id).child(field).set(value)

    def set_category_emissions(self, user_id: str, category: str, annual_emissions_tons: float) -> None:
        self._db.child("surveys").child(category).child(user_id).update({"annualEmissions": annual_emissions_tons})

    def read_category_emissions(self, user_id: str, category: str) -> float:
        snap = self._db.child("surveys").child(category).child(user_id).child("annualEmissions").get()
        val = snap.val() if snap else None
        try:
            return float(val) if val is not None else 0.0
        except Exception:  # noqa: BLE001
            return 0.0


class SplashScreen(Screen):
    def on_enter(self) -> None:
        Clock.schedule_once(lambda *_: App.get_running_app().switch_screen("landing"), 3)


class LandingScreen(Screen):
    pass


class SignUpScreen(Screen):
    status_text = StringProperty("")

    def handle_create_account(self) -> None:
        app = App.get_running_app()
        first_name: str = self.ids.first_name.text.strip()
        last_name: str = self.ids.last_name.text.strip()
        email: str = self.ids.email.text.strip()
        password: str = self.ids.password.text.strip()
        confirm: str = self.ids.confirm_password.text.strip()

        if not (first_name and last_name and email and password and confirm):
            self.status_text = "All fields are required."
            return
        if password != confirm:
            self.status_text = "Passwords do not match."
            return
        if len(password) < 8:
            self.status_text = "Password must be at least 8 characters."
            return

        self.status_text = "Creating account..."

        def _task() -> None:
            try:
                result = app.firebase.create_user(email, password)
                user_id = result.get("localId")
                profile = {
                    "firstName": first_name,
                    "lastName": last_name,
                    "email": email,
                    "totalCO2Saved": 0.0,
                    "taliPoints": 0,
                }
                app.firebase.set_user_profile(user_id, profile)
                # Mirror Android structure: users/{uid}/survey_completed (bool)
                app.firebase.set_user_field(user_id, "survey_completed", False)
                Clock.schedule_once(lambda *_: self._on_success(), 0)
            except Exception as exc:  # noqa: BLE001
                Clock.schedule_once(lambda *_: self._on_error(str(exc)), 0)

        threading.Thread(target=_task, daemon=True).start()

    def _on_success(self) -> None:
        self.status_text = "Account created. Redirecting to Sign In..."
        Clock.schedule_once(lambda *_: App.get_running_app().switch_screen("signin"), 0.7)

    def _on_error(self, msg: str) -> None:
        self.status_text = f"Failed: {msg}"[:200]


class SignInScreen(Screen):
    status_text = StringProperty("")

    def handle_login(self) -> None:
        app = App.get_running_app()
        email: str = self.ids.signin_email.text.strip()
        password: str = self.ids.signin_password.text.strip()
        if not (email and password):
            self.status_text = "Please fill in both fields."
            return
        if len(password) < 8:
            self.status_text = "Password must be at least 8 characters."
            return

        self.status_text = "Signing in..."

        def _task() -> None:
            try:
                app.firebase.sign_in(email, password)
                Clock.schedule_once(lambda *_: self._on_success(), 0)
            except Exception as exc:  # noqa: BLE001
                Clock.schedule_once(lambda *_: self._on_error(str(exc)), 0)

        threading.Thread(target=_task, daemon=True).start()

    def _on_success(self) -> None:
        self.status_text = "Signed in."
        Clock.schedule_once(lambda *_: App.get_running_app().switch_screen("home"), 0.5)

    def _on_error(self, msg: str) -> None:
        self.status_text = f"Incorrect email or password. {msg}"[:200]


class HomeScreen(Screen):
    def on_pre_enter(self) -> None:
        pass

    def open_survey_or_overall(self) -> None:
        app = App.get_running_app()
        user_id = app.firebase.get_current_user_id() or "anonymous"

        def _task() -> None:
            try:
                value = app.firebase.get_user_field(user_id, "survey_completed")
                screen_name = "overall" if (value is True) else "survey"
                Clock.schedule_once(lambda *_: app.switch_screen(screen_name), 0)
            except Exception:
                Clock.schedule_once(lambda *_: app.switch_screen("survey"), 0)

        threading.Thread(target=_task, daemon=True).start()


class DashboardScreen(Screen):
    user_name = StringProperty("User")
    month_year = StringProperty("")
    co2_saved_text = StringProperty("0.00 kg CO2e saved")
    tali_points_text = StringProperty("0")

    home_tons = NumericProperty(0.0)
    travel_tons = NumericProperty(0.0)
    food_tons = NumericProperty(0.0)
    others_tons = NumericProperty(0.0)

    def on_pre_enter(self) -> None:
        self.month_year = datetime.now().strftime("%B %Y")
        self._load_user_and_emissions()

    def _load_user_and_emissions(self) -> None:
        app = App.get_running_app()
        user_id = app.firebase.get_current_user_id()
        if not user_id:
            return

        def _task() -> None:
            try:
                profile = app.firebase.read_user(user_id) or {}
                first_name = profile.get("firstName", "User")
                co2_value = profile.get("totalCO2Saved", 0.0) or 0.0
                tali_points = profile.get("taliPoints", 0) or 0

                home = app.firebase.read_category_emissions(user_id, "home")
                travel = app.firebase.read_category_emissions(user_id, "travel")
                food = app.firebase.read_category_emissions(user_id, "food")
                others = app.firebase.read_category_emissions(user_id, "others")

                def _apply() -> None:
                    self.user_name = first_name
                    self.co2_saved_text = f"{co2_value:.2f} kg CO2e saved"
                    self.tali_points_text = str(int(tali_points))
                    self.home_tons = home
                    self.travel_tons = travel
                    self.food_tons = food
                    self.others_tons = others
                    self._update_bars()

                Clock.schedule_once(lambda *_: _apply(), 0)
            except Exception as exc:  # noqa: BLE001
                print(f"[Dashboard] Failed to load: {exc}")

        threading.Thread(target=_task, daemon=True).start()

    def _bar_height_dp(self, tons: float) -> float:
        if tons <= 0.5:
            height_dp = 100.0 * (tons / 0.5)
        elif tons <= 1.0:
            height_dp = 200.0 * (tons / 1.0)
        elif tons <= 1.5:
            height_dp = 300.0 * (tons / 1.5)
        elif tons <= 2.0:
            height_dp = 400.0 * (tons / 2.0)
        elif tons <= 2.5:
            height_dp = 500.0 * (tons / 2.5)
        else:
            height_dp = 600.0 * (tons / 3.0)
        if height_dp < 10.0:
            height_dp = 10.0
        if height_dp > 600.0:
            height_dp = 600.0
        return height_dp

    def _update_bars(self) -> None:
        self.ids.home_bar.height = dp(self._bar_height_dp(self.home_tons))
        self.ids.travel_bar.height = dp(self._bar_height_dp(self.travel_tons))
        self.ids.food_bar.height = dp(self._bar_height_dp(self.food_tons))
        self.ids.others_bar.height = dp(self._bar_height_dp(self.others_tons))


class TakeSurveyScreen(Screen):
    status_text = StringProperty("")

    def save_survey(self) -> None:
        app = App.get_running_app()
        user_id = app.firebase.get_current_user_id()
        if not user_id:
            self.status_text = "Please sign in first."
            return

        def _float_from(idname: str) -> float:
            try:
                return float(self.ids[idname].text.strip() or "0")
            except Exception:
                return 0.0

        home = _float_from("home_input")
        travel = _float_from("travel_input")
        food = _float_from("food_input")
        others = _float_from("others_input")

        self.status_text = "Saving..."

        def _task() -> None:
            try:
                app.firebase.set_category_emissions(user_id, "home", home)
                app.firebase.set_category_emissions(user_id, "travel", travel)
                app.firebase.set_category_emissions(user_id, "food", food)
                app.firebase.set_category_emissions(user_id, "others", others)
                app.firebase.set_user_field(user_id, "survey_completed", True)
                Clock.schedule_once(lambda *_: self._on_done(), 0)
            except Exception as exc:  # noqa: BLE001
                Clock.schedule_once(lambda *_: self._on_error(str(exc)), 0)

        threading.Thread(target=_task, daemon=True).start()

    def _on_done(self) -> None:
        self.status_text = "Survey saved."
        Clock.schedule_once(lambda *_: App.get_running_app().switch_screen("overall"), 0.6)

    def _on_error(self, msg: str) -> None:
        self.status_text = f"Failed: {msg}"[:200]


class OverallScreen(Screen):
    total_text = StringProperty("0.0 tons")

    def on_pre_enter(self) -> None:
        app = App.get_running_app()
        user_id = app.firebase.get_current_user_id()
        if not user_id:
            self.total_text = "Sign in required"
            return

        def _task() -> None:
            try:
                home = app.firebase.read_category_emissions(user_id, "home")
                travel = app.firebase.read_category_emissions(user_id, "travel")
                food = app.firebase.read_category_emissions(user_id, "food")
                others = app.firebase.read_category_emissions(user_id, "others")
                total = home + travel + food + others
                Clock.schedule_once(lambda *_: self._apply(total), 0)
            except Exception as exc:  # noqa: BLE001
                Clock.schedule_once(lambda *_: self._apply_error(str(exc)), 0)

        threading.Thread(target=_task, daemon=True).start()

    def _apply(self, total: float) -> None:
        self.total_text = f"{total:.2f} tons annual emissions"

    def _apply_error(self, msg: str) -> None:
        self.total_text = f"Failed to load: {msg}"[:200]


class ProgressScreen(Screen):
    pass


class SearchScreen(Screen):
    pass


class ActionScreen(Screen):
    title = StringProperty("")
    completed = BooleanProperty(False)

    def complete_action(self) -> None:
        self.completed = True
        app = App.get_running_app()
        user_id = app.firebase.get_current_user_id()
        if not user_id:
            return

        def _task() -> None:
            try:
                profile = app.firebase.read_user(user_id) or {}
                tali_points = int(profile.get("taliPoints", 0)) + 10
                app.firebase.update_user_fields(user_id, {"taliPoints": tali_points})
            except Exception as exc:  # noqa: BLE001
                print(f"[Action] update failed: {exc}")

        threading.Thread(target=_task, daemon=True).start()


class HabitScreen(Screen):
    pass


class ConnectScreen(Screen):
    pass


class RootManager(ScreenManager):
    pass


KV_FILE = os.path.join(os.path.dirname(__file__), "app.kv")


class CarbonfootprintApp(App):
    firebase: FirebaseService = ObjectProperty(None)

    def build(self):  # noqa: D401
        """Build and return the root widget."""
        Builder.load_file(KV_FILE)
        sm = RootManager(transition=NoTransition())
        return sm

    def on_start(self) -> None:
        config_path = os.path.join(os.path.dirname(__file__), "firebase_config.json")
        self.firebase = FirebaseService(config_path)

    def switch_screen(self, name: str) -> None:
        self.root.current = name


if __name__ == "__main__":
    CarbonfootprintApp().run()