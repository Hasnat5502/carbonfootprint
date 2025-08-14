from flask import Flask, render_template, request, redirect, url_for, session
from datetime import datetime

app = Flask(__name__)
app.secret_key = "dev-secret-key"

# In-memory store to simulate persistence (per-session)
USERS = {"demo@example.com": {"first_name": "Demo", "password": "demo123"}}
SURVEYS = {}


def get_current_user_id() -> str:
	user_email = session.get("user_email")
	return user_email or "anonymous"


@app.route("/")
def index():
	if session.get("user_email"):
		return redirect(url_for("dashboard"))
	return render_template("index.html")


@app.route("/login", methods=["GET", "POST"])
def login():
	if request.method == "POST":
		email = request.form.get("email", "").strip().lower()
		password = request.form.get("password", "")
		user = USERS.get(email)
		if user and user.get("password") == password:
			session["user_email"] = email
			return redirect(url_for("dashboard"))
		return render_template("login.html", error="Invalid email or password")
	return render_template("login.html")


@app.route("/logout")
def logout():
	session.clear()
	return redirect(url_for("index"))


@app.route("/signup", methods=["GET", "POST"])
def signup():
	if request.method == "POST":
		email = request.form.get("email", "").strip().lower()
		first_name = request.form.get("first_name", "").strip()
		password = request.form.get("password", "")
		if not email or not first_name or not password:
			return render_template("signup.html", error="All fields are required")
		if email in USERS:
			return render_template("signup.html", error="Account already exists")
		USERS[email] = {"first_name": first_name, "password": password, "total_co2_saved": 0.0, "tali_points": 0}
		session["user_email"] = email
		return redirect(url_for("dashboard"))
	return render_template("signup.html")


@app.route("/dashboard")
def dashboard():
	user_id = get_current_user_id()
	user = USERS.get(user_id, {"first_name": "User", "total_co2_saved": 0.0, "tali_points": 0})
	today = datetime.now().strftime("%B %Y")

	# Gather category emissions from SURVEYS
	user_surveys = SURVEYS.get(user_id, {})
	home_emission = float(user_surveys.get("home", {}).get("annualEmissions", 0.0))
	travel_emission = float(user_surveys.get("travel", {}).get("annualEmissions", 0.0))
	food_emission = float(user_surveys.get("food", {}).get("annualEmissions", 0.0))
	others_emission = float(user_surveys.get("others", {}).get("annualEmissions", 0.0))

	return render_template(
		"dashboard.html",
		first_name=user.get("first_name", "User"),
		month_year=today,
		co2_saved=user.get("total_co2_saved", 0.0),
		tali_points=user.get("tali_points", 0),
		home_emission=home_emission,
		travel_emission=travel_emission,
		food_emission=food_emission,
		others_emission=others_emission,
	)


@app.route("/overall")
def overall():
	user_id = get_current_user_id()
	user_surveys = SURVEYS.get(user_id, {})
	home = float(user_surveys.get("home", {}).get("annualEmissions", 0.0))
	travel = float(user_surveys.get("travel", {}).get("annualEmissions", 0.0))
	food = float(user_surveys.get("food", {}).get("annualEmissions", 0.0))
	others = float(user_surveys.get("others", {}).get("annualEmissions", 0.0))
	total = home + travel + food + others

	def describe(total_tons: float) -> str:
		if total_tons <= 0:
			return "Complete surveys to calculate your carbon impact"
		billboards = max(1, int((total_tons * 3) / 18 + 0.999))
		return f"{total_tons:.1f} tons of CO2e would melt an area of arctic sea ice the size of {billboards} {'billboard' if billboards == 1 else 'billboards'}"

	return render_template(
		"overall.html",
		home=home,
		travel=travel,
		food=food,
		others=others,
		total=total,
		description=describe(total),
	)


@app.route("/survey/travel", methods=["GET", "POST"])
def survey_travel():
	if request.method == "POST":
		# Map radio-like inputs to emission values, mirroring TravelServey.java
		distance = request.form.get("distance", "noDrive")
		transport = request.form.get("transport", "walk")
		vehicle_type = request.form.get("vehicleType", "noVehicle")
		flights = request.form.get("flights", "noFlights")
		carpool = request.form.get("carpool", "alwaysCarpool")
		ride_hailing = request.form.get("rideHailing", "neverRide")
		route_planning = request.form.get("routePlanning", "yesPlan")

		def distance_emission(val: str) -> float:
			return {
				"noDrive": 0.0,
				"low": 2.0,
				"medium": 5.0,
				"high": 12.0,
			}.get(val, 0.0)

		def transport_emission(val: str) -> float:
			return {
				"walk": 0.0,
				"bicycle": 0.1,
				"publicTransport": 1.2,
				"motorcycle": 3.5,
				"car": 8.0,
			}.get(val, 0.0)

		def vehicle_type_emission(val: str) -> float:
			return {
				"electric": 3.0,
				"hybrid": 5.5,
				"petrol": 8.0,
				"diesel": 7.2,
				"noVehicle": 0.0,
			}.get(val, 0.0)

		def flights_emission(val: str) -> float:
			return {
				"noFlights": 0.0,
				"few": 2.0,
				"moderate": 5.0,
				"many": 10.0,
			}.get(val, 0.0)

		def carpool_emission(val: str) -> float:
			return {
				"alwaysCarpool": 0.3,
				"sometimesCarpool": 0.6,
				"rarelyCarpool": 0.9,
				"neverCarpool": 1.0,
			}.get(val, 0.0)

		def ride_hailing_emission(val: str) -> float:
			return {
				"neverRide": 0.0,
				"occasionally": 0.8,
				"weekly": 2.5,
				"daily": 6.0,
			}.get(val, 0.0)

		def route_planning_emission(val: str) -> float:
			return {
				"yesPlan": 0.9,
				"sometimesPlan": 1.0,
				"noPlan": 1.1,
			}.get(val, 0.0)

		distance_em = distance_emission(distance)
		transport_em = transport_emission(transport)
		vehicle_em = vehicle_type_emission(vehicle_type)
		flights_em = flights_emission(flights)
		carpool_em = carpool_emission(carpool)
		ride_hailing_em = ride_hailing_emission(ride_hailing)
		route_planning_em = route_planning_emission(route_planning)

		weekly = (
			distance_em + transport_em + vehicle_em + flights_em +
			carpool_em + ride_hailing_em + route_planning_em
		)
		annual_tons = (weekly * 52) / 1000.0

		user_id = get_current_user_id()
		SURVEYS.setdefault(user_id, {})["travel"] = {
			"distance": distance,
			"transport": transport,
			"vehicleType": vehicle_type,
			"flights": flights,
			"carpool": carpool,
			"rideHailing": ride_hailing,
			"routePlanning": route_planning,
			"distanceEmission": distance_em,
			"transportEmission": transport_em,
			"vehicleTypeEmission": vehicle_em,
			"flightsEmission": flights_em,
			"carpoolEmission": carpool_em,
			"rideHailingEmission": ride_hailing_em,
			"routePlanningEmission": route_planning_em,
			"weeklyEmissions": weekly,
			"annualEmissions": annual_tons,
		}
		return redirect(url_for("overall"))

	return render_template("survey_travel.html")


if __name__ == "__main__":
	app.run(host="0.0.0.0", port=8000, debug=True)