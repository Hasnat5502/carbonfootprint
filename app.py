from flask import Flask, render_template, request, redirect, url_for, flash, session, jsonify
from flask_sqlalchemy import SQLAlchemy
from werkzeug.security import generate_password_hash, check_password_hash
from datetime import datetime
import os

app = Flask(__name__)
app.config['SECRET_KEY'] = 'your-secret-key-here'
app.config['SQLALCHEMY_DATABASE_URI'] = 'sqlite:///carbon_footprint.db'
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False

db = SQLAlchemy(app)

# Database Models
class User(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    email = db.Column(db.String(120), unique=True, nullable=False)
    password_hash = db.Column(db.String(200), nullable=False)
    first_name = db.Column(db.String(50), nullable=False)
    last_name = db.Column(db.String(50), nullable=False)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    total_co2_saved = db.Column(db.Float, default=0.0)
    tali_points = db.Column(db.Integer, default=0)

class Survey(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, db.ForeignKey('user.id'), nullable=False)
    survey_type = db.Column(db.String(20), nullable=False)  # home, travel, food, others
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    co2_emission = db.Column(db.Float, default=0.0)

class Action(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, db.ForeignKey('user.id'), nullable=False)
    action_type = db.Column(db.String(50), nullable=False)
    completed_at = db.Column(db.DateTime, default=datetime.utcnow)
    co2_saved = db.Column(db.Float, default=0.0)

# Routes
@app.route('/')
def index():
    return render_template('index.html')

@app.route('/test')
def test():
    """Test route to check if the application is working"""
    return jsonify({
        'status': 'success',
        'message': 'Application is running',
        'database': 'connected' if db.engine else 'not connected'
    })

@app.route('/signup', methods=['GET', 'POST'])
def signup():
    if request.method == 'POST':
        print("Signup POST request received")
        print(f"Form data: {request.form}")
        
        email = request.form['email']
        password = request.form['password']
        confirm_password = request.form.get('confirm_password', '')
        first_name = request.form['first_name']
        last_name = request.form['last_name']
        
        # Validation
        if not email or not password or not first_name or not last_name:
            flash('All fields are required')
            return redirect(url_for('signup'))
        
        if password != confirm_password:
            flash('Passwords do not match')
            return redirect(url_for('signup'))
        
        if len(password) < 6:
            flash('Password must be at least 6 characters long')
            return redirect(url_for('signup'))
        
        if User.query.filter_by(email=email).first():
            flash('Email already registered')
            return redirect(url_for('signup'))
        
        try:
            user = User(
                email=email,
                password_hash=generate_password_hash(password),
                first_name=first_name,
                last_name=last_name
            )
            db.session.add(user)
            db.session.commit()
            
            flash('Registration successful! Please sign in.')
            return redirect(url_for('signin'))
        except Exception as e:
            db.session.rollback()
            flash('Registration failed. Please try again.')
            print(f"Registration error: {e}")
            return redirect(url_for('signup'))
    
    return render_template('signup.html')

@app.route('/signin', methods=['GET', 'POST'])
def signin():
    if request.method == 'POST':
        email = request.form.get('email', '').strip()
        password = request.form.get('password', '')
        
        # Validation
        if not email or not password:
            flash('Please enter both email and password')
            return redirect(url_for('signin'))
        
        try:
            user = User.query.filter_by(email=email).first()
            if user and check_password_hash(user.password_hash, password):
                session['user_id'] = user.id
                session['user_name'] = user.first_name
                flash(f'Welcome back, {user.first_name}!')
                return redirect(url_for('dashboard'))
            else:
                flash('Invalid email or password')
        except Exception as e:
            flash('Login failed. Please try again.')
            print(f"Login error: {e}")
    
    return render_template('signin.html')

@app.route('/logout')
def logout():
    session.clear()
    return redirect(url_for('index'))

@app.route('/dashboard')
def dashboard():
    if 'user_id' not in session:
        return redirect(url_for('signin'))
    
    user = User.query.get(session['user_id'])
    
    # Get survey data for charts
    surveys = Survey.query.filter_by(user_id=user.id).all()
    home_emission = sum([s.co2_emission for s in surveys if s.survey_type == 'home'])
    travel_emission = sum([s.co2_emission for s in surveys if s.survey_type == 'travel'])
    food_emission = sum([s.co2_emission for s in surveys if s.survey_type == 'food'])
    others_emission = sum([s.co2_emission for s in surveys if s.survey_type == 'others'])
    
    return render_template('dashboard.html', 
                         user=user, 
                         home_emission=home_emission,
                         travel_emission=travel_emission,
                         food_emission=food_emission,
                         others_emission=others_emission)

@app.route('/survey/<survey_type>', methods=['GET', 'POST'])
def survey(survey_type):
    if 'user_id' not in session:
        return redirect(url_for('signin'))
    
    if request.method == 'POST':
        # Calculate CO2 emission based on survey type and answers
        co2_emission = calculate_emission(survey_type, request.form)
        
        survey = Survey(
            user_id=session['user_id'],
            survey_type=survey_type,
            co2_emission=co2_emission
        )
        db.session.add(survey)
        db.session.commit()
        
        flash(f'{survey_type.title()} survey completed!')
        return redirect(url_for('dashboard'))
    
    return render_template(f'survey_{survey_type}.html')

@app.route('/actions')
def actions():
    if 'user_id' not in session:
        return redirect(url_for('signin'))
    
    return render_template('actions.html')

@app.route('/complete_action', methods=['POST'])
def complete_action():
    if 'user_id' not in session:
        return jsonify({'error': 'Not authenticated'})
    
    action_type = request.form['action_type']
    co2_saved = float(request.form['co2_saved'])
    
    action = Action(
        user_id=session['user_id'],
        action_type=action_type,
        co2_saved=co2_saved
    )
    db.session.add(action)
    
    # Update user's total CO2 saved and tali points
    user = User.query.get(session['user_id'])
    user.total_co2_saved += co2_saved
    user.tali_points += int(co2_saved * 10)  # 10 points per kg CO2 saved
    
    db.session.commit()
    
    return jsonify({'success': True, 'co2_saved': co2_saved})

@app.route('/progress')
def progress():
    if 'user_id' not in session:
        return redirect(url_for('signin'))
    
    user = User.query.get(session['user_id'])
    actions = Action.query.filter_by(user_id=user.id).order_by(Action.completed_at.desc()).limit(10).all()
    
    return render_template('progress.html', user=user, actions=actions)

def calculate_emission(survey_type, form_data):
    """Calculate CO2 emission based on survey answers"""
    if survey_type == 'home':
        return calculate_home_emission(form_data)
    elif survey_type == 'travel':
        return calculate_travel_emission(form_data)
    elif survey_type == 'food':
        return calculate_food_emission(form_data)
    elif survey_type == 'others':
        return calculate_others_emission(form_data)
    return 0.0

def calculate_home_emission(form_data):
    emission = 0.0
    
    # Home size impact
    home_size = form_data.get('home_size', 'medium')
    if home_size == 'small':
        emission += 2.0
    elif home_size == 'medium':
        emission += 4.0
    elif home_size == 'large':
        emission += 6.0
    
    # Heating system impact
    heating = form_data.get('heating', 'gas')
    if heating == 'electric':
        emission += 3.0
    elif heating == 'gas':
        emission += 2.5
    elif heating == 'oil':
        emission += 4.0
    elif heating == 'heat_pump':
        emission += 1.0
    elif heating == 'none':
        emission += 0.0
    
    # Air conditioning impact
    ac_usage = form_data.get('ac_usage', 'moderate')
    if ac_usage == 'heavy':
        emission += 3.0
    elif ac_usage == 'moderate':
        emission += 1.5
    elif ac_usage == 'light':
        emission += 0.5
    elif ac_usage == 'none':
        emission += 0.0
    
    # Energy efficiency impact
    efficiency = form_data.get('energy_efficiency', 'somewhat')
    if efficiency == 'very':
        emission -= 1.0
    elif efficiency == 'somewhat':
        emission += 0.0
    elif efficiency == 'not':
        emission += 2.0
    
    # Renewable energy impact
    renewable = form_data.get('renewable_energy', 'none')
    if renewable == 'solar':
        emission -= 2.0
    elif renewable == 'wind':
        emission -= 2.0
    elif renewable == 'green':
        emission -= 1.0
    elif renewable == 'none':
        emission += 0.0
    
    # Water usage impact
    water_usage = form_data.get('water_usage', 'average')
    if water_usage == 'conservative':
        emission -= 0.5
    elif water_usage == 'average':
        emission += 0.0
    elif water_usage == 'high':
        emission += 1.0
    
    return max(0.0, emission)

def calculate_travel_emission(form_data):
    emission = 0.0
    
    # Commute distance impact
    commute_distance = form_data.get('commute_distance', 'medium')
    if commute_distance == 'none':
        emission += 0.0
    elif commute_distance == 'short':
        emission += 1.0
    elif commute_distance == 'medium':
        emission += 3.0
    elif commute_distance == 'long':
        emission += 6.0
    
    # Primary transportation impact
    transport = form_data.get('primary_transport', 'car')
    if transport == 'walking':
        emission += 0.0
    elif transport == 'bicycle':
        emission += 0.1
    elif transport == 'public':
        emission += 1.0
    elif transport == 'motorcycle':
        emission += 2.0
    elif transport == 'car':
        emission += 4.0
    
    # Vehicle type impact
    vehicle_type = form_data.get('vehicle_type', 'petrol')
    if vehicle_type == 'electric':
        emission += 1.5
    elif vehicle_type == 'hybrid':
        emission += 2.5
    elif vehicle_type == 'petrol':
        emission += 4.0
    elif vehicle_type == 'diesel':
        emission += 3.5
    elif vehicle_type == 'none':
        emission += 0.0
    
    # Air travel impact
    air_travel = form_data.get('air_travel', 'few')
    if air_travel == 'none':
        emission += 0.0
    elif air_travel == 'few':
        emission += 1.0
    elif air_travel == 'moderate':
        emission += 3.0
    elif air_travel == 'frequent':
        emission += 6.0
    
    # Carpooling impact
    carpooling = form_data.get('carpooling', 'sometimes')
    if carpooling == 'always':
        emission -= 1.0
    elif carpooling == 'sometimes':
        emission -= 0.5
    elif carpooling == 'rarely':
        emission += 0.0
    elif carpooling == 'never':
        emission += 0.5
    
    # Ride sharing impact
    ride_sharing = form_data.get('ride_sharing', 'occasional')
    if ride_sharing == 'frequent':
        emission += 1.5
    elif ride_sharing == 'occasional':
        emission += 0.5
    elif ride_sharing == 'rarely':
        emission += 0.0
    elif ride_sharing == 'never':
        emission += 0.0
    
    # Route planning impact
    route_planning = form_data.get('route_planning', 'sometimes')
    if route_planning == 'eco':
        emission -= 0.5
    elif route_planning == 'sometimes':
        emission -= 0.2
    elif route_planning == 'fastest':
        emission += 0.5
    elif route_planning == 'none':
        emission += 0.0
    
    return max(0.0, emission)

def calculate_food_emission(form_data):
    emission = 0.0
    
    # Meat consumption impact
    meat_frequency = form_data.get('meat_frequency', 'three_four')
    if meat_frequency == 'never':
        emission += 0.0
    elif meat_frequency == 'once_twice':
        emission += 1.0
    elif meat_frequency == 'three_four':
        emission += 2.5
    elif meat_frequency == 'daily':
        emission += 4.0
    
    # Vegetarian days impact
    vegetarian_days = form_data.get('vegetarian_days', '1-2')
    if vegetarian_days == '0':
        emission += 2.0
    elif vegetarian_days == '1-2':
        emission += 1.5
    elif vegetarian_days == '3-5':
        emission += 0.8
    elif vegetarian_days == '6-7':
        emission += 0.2
    
    # Food purchase location impact
    food_purchase = form_data.get('food_purchase', 'supermarket')
    if food_purchase == 'local':
        emission += 0.5
    elif food_purchase == 'supermarket':
        emission += 1.0
    elif food_purchase == 'imported':
        emission += 2.0
    elif food_purchase == 'online':
        emission += 1.5
    
    # Organic produce impact
    organic_produce = form_data.get('organic_produce', 'sometimes')
    if organic_produce == 'always':
        emission -= 0.5
    elif organic_produce == 'sometimes':
        emission -= 0.2
    elif organic_produce == 'rarely':
        emission += 0.0
    elif organic_produce == 'never':
        emission += 0.5
    
    # Eating out impact
    eat_out = form_data.get('eat_out_frequency', 'sometimes')
    if eat_out == 'never':
        emission += 0.0
    elif eat_out == 'rarely':
        emission += 0.5
    elif eat_out == 'sometimes':
        emission += 1.0
    elif eat_out == 'frequently':
        emission += 2.0
    
    # Food waste impact
    food_waste = form_data.get('food_waste', 'little')
    if food_waste == 'none':
        emission -= 0.5
    elif food_waste == 'little':
        emission -= 0.2
    elif food_waste == 'some':
        emission += 0.5
    elif food_waste == 'lots':
        emission += 1.0
    
    # Reusable items impact
    reusable_items = form_data.get('reusable_items', 'sometimes')
    if reusable_items == 'always':
        emission -= 0.5
    elif reusable_items == 'mostly':
        emission -= 0.3
    elif reusable_items == 'sometimes':
        emission -= 0.1
    elif reusable_items == 'rarely':
        emission += 0.5
    
    return max(0.0, emission)

def calculate_others_emission(form_data):
    emission = 0.0
    
    # Shopping frequency impact
    shopping_frequency = form_data.get('shopping_frequency', 'occasionally')
    if shopping_frequency == 'rarely':
        emission += 0.5
    elif shopping_frequency == 'occasionally':
        emission += 1.0
    elif shopping_frequency == 'monthly':
        emission += 2.0
    elif shopping_frequency == 'weekly':
        emission += 3.0
    
    # Second-hand shopping impact
    second_hand = form_data.get('second_hand', 'sometimes')
    if second_hand == 'always':
        emission -= 1.0
    elif second_hand == 'often':
        emission -= 0.5
    elif second_hand == 'sometimes':
        emission -= 0.2
    elif second_hand == 'never':
        emission += 0.5
    
    # Waste management impact
    waste_management = form_data.get('waste_management', 'recycle_only')
    if waste_management == 'zero':
        emission -= 1.0
    elif waste_management == 'recycle_compost':
        emission -= 0.5
    elif waste_management == 'recycle_only':
        emission -= 0.2
    elif waste_management == 'no_recycling':
        emission += 1.0
    
    # Device upgrades impact
    device_upgrades = form_data.get('device_upgrades', 'sometimes')
    if device_upgrades == 'never':
        emission += 0.0
    elif device_upgrades == 'rarely':
        emission += 0.5
    elif device_upgrades == 'sometimes':
        emission += 1.0
    elif device_upgrades == 'frequently':
        emission += 2.0
    
    # Entertainment activities impact
    entertainment = form_data.get('entertainment', 'mixed')
    if entertainment == 'outdoor':
        emission += 0.0
    elif entertainment == 'cultural':
        emission += 0.5
    elif entertainment == 'home':
        emission += 1.0
    elif entertainment == 'mixed':
        emission += 0.5
    
    # Energy consciousness impact
    energy_consciousness = form_data.get('energy_consciousness', 'somewhat')
    if energy_consciousness == 'very':
        emission -= 0.5
    elif energy_consciousness == 'somewhat':
        emission -= 0.2
    elif energy_consciousness == 'not':
        emission += 0.5
    
    # Sustainable products impact
    sustainable_products = form_data.get('sustainable_products', 'sometimes')
    if sustainable_products == 'always':
        emission -= 0.5
    elif sustainable_products == 'often':
        emission -= 0.3
    elif sustainable_products == 'sometimes':
        emission -= 0.1
    elif sustainable_products == 'rarely':
        emission += 0.5
    
    return max(0.0, emission)

if __name__ == '__main__':
    with app.app_context():
        try:
            db.create_all()
            print("Database tables created successfully!")
        except Exception as e:
            print(f"Database creation error: {e}")
    app.run(debug=True, host='0.0.0.0', port=5000)