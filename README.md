# EcoTracker - Carbon Footprint Tracking Web Application

A comprehensive web application built with Flask, HTML, CSS, and JavaScript that helps users track their carbon footprint and make sustainable choices. This is a web-based replication of the original Android Carbon Footprint application.

## Features

### 🏠 **Home Energy Survey**
- Assess home size and energy consumption
- Evaluate heating and cooling systems
- Analyze energy efficiency measures
- Consider renewable energy usage
- Monitor water consumption patterns

### 🚗 **Travel & Transportation Survey**
- Track daily commute distances
- Evaluate primary transportation methods
- Assess vehicle types and fuel efficiency
- Monitor air travel frequency
- Analyze carpooling and ride-sharing habits
- Consider route planning strategies

### 🍽️ **Food & Diet Survey**
- Evaluate meat consumption patterns
- Track vegetarian meal frequency
- Assess food purchase locations
- Monitor organic produce consumption
- Analyze eating out frequency
- Evaluate food waste management
- Consider reusable item usage

### 🔄 **Other Activities Survey**
- Assess shopping habits and frequency
- Evaluate second-hand shopping practices
- Monitor waste management strategies
- Track digital device upgrade patterns
- Analyze entertainment preferences
- Consider energy conservation awareness
- Evaluate sustainable product choices

### 📊 **Dashboard & Analytics**
- Interactive charts showing carbon footprint breakdown
- Real-time progress tracking
- CO2 savings visualization
- Tali points system for gamification
- Progress bars for different emission categories

### ✅ **Action System**
- Complete sustainable actions to reduce carbon footprint
- Earn points for completed actions
- Track action history and achievements
- Categorized actions by impact area:
  - Home Energy
  - Transportation
  - Food & Diet
  - Waste & Recycling

### 🏆 **Progress Tracking**
- Achievement badges system
- Historical action tracking
- CO2 savings over time visualization
- Personal statistics and milestones

## Technology Stack

- **Backend**: Python Flask
- **Database**: SQLite with SQLAlchemy ORM
- **Frontend**: HTML5, CSS3, JavaScript
- **UI Framework**: Bootstrap 5
- **Charts**: Chart.js
- **Icons**: Font Awesome
- **Authentication**: Session-based with password hashing

## Installation & Setup

### Prerequisites
- Python 3.7 or higher
- pip (Python package installer)

### Step 1: Clone the Repository
```bash
git clone <repository-url>
cd carbon-footprint-tracker
```

### Step 2: Create Virtual Environment (Recommended)
```bash
python -m venv venv

# On Windows
venv\Scripts\activate

# On macOS/Linux
source venv/bin/activate
```

### Step 3: Install Dependencies
```bash
pip install -r requirements.txt
```

### Step 4: Run the Application
```bash
python app.py
```

### Step 5: Access the Application
Open your web browser and navigate to:
```
http://localhost:5000
```

## Project Structure

```
carbon-footprint-tracker/
├── app.py                 # Main Flask application
├── requirements.txt       # Python dependencies
├── README.md             # Project documentation
├── templates/            # HTML templates
│   ├── base.html         # Base template with navigation
│   ├── index.html        # Landing page
│   ├── signup.html       # User registration
│   ├── signin.html       # User login
│   ├── dashboard.html    # Main dashboard
│   ├── survey_home.html  # Home energy survey
│   ├── survey_travel.html # Travel survey
│   ├── survey_food.html  # Food survey
│   ├── survey_others.html # Other activities survey
│   ├── actions.html      # Sustainable actions page
│   └── progress.html     # Progress tracking page
├── static/               # Static files
│   ├── css/
│   │   └── style.css     # Custom styles
│   ├── js/
│   │   └── main.js       # JavaScript utilities
│   └── images/           # Image assets
└── carbon_footprint.db   # SQLite database (created automatically)
```

## Database Schema

### Users Table
- `id`: Primary key
- `email`: User email (unique)
- `password_hash`: Hashed password
- `first_name`: User's first name
- `last_name`: User's last name
- `created_at`: Account creation timestamp
- `total_co2_saved`: Total CO2 saved through actions
- `tali_points`: Points earned through sustainable actions

### Surveys Table
- `id`: Primary key
- `user_id`: Foreign key to users table
- `survey_type`: Type of survey (home, travel, food, others)
- `created_at`: Survey completion timestamp
- `co2_emission`: Calculated CO2 emission from survey

### Actions Table
- `id`: Primary key
- `user_id`: Foreign key to users table
- `action_type`: Type of sustainable action
- `completed_at`: Action completion timestamp
- `co2_saved`: CO2 saved by completing the action

## Usage Guide

### 1. Getting Started
1. Visit the landing page and click "Get Started"
2. Create a new account with your email and password
3. Sign in to access your dashboard

### 2. Taking Surveys
1. From the dashboard, click on any survey type (Home, Travel, Food, Others)
2. Answer all questions honestly based on your current lifestyle
3. Submit the survey to see your carbon footprint assessment

### 3. Taking Actions
1. Navigate to the "Actions" page
2. Browse through different categories of sustainable actions
3. Click "Complete" on actions you've performed
4. Earn points and reduce your carbon footprint

### 4. Tracking Progress
1. Visit the "Progress" page to see your achievements
2. View your action history and CO2 savings over time
3. Unlock achievement badges as you reach milestones

## Carbon Footprint Calculation

The application uses a comprehensive algorithm to calculate carbon emissions based on:

### Home Energy Factors
- Home size and energy consumption
- Heating and cooling systems
- Energy efficiency measures
- Renewable energy usage
- Water consumption patterns

### Transportation Factors
- Daily commute distance and method
- Vehicle type and fuel efficiency
- Air travel frequency
- Carpooling and ride-sharing habits
- Route planning strategies

### Food & Diet Factors
- Meat consumption frequency
- Vegetarian meal patterns
- Food purchase locations
- Organic produce consumption
- Eating out frequency
- Food waste management
- Reusable item usage

### Other Lifestyle Factors
- Shopping habits and frequency
- Second-hand shopping practices
- Waste management strategies
- Digital device upgrade patterns
- Entertainment preferences
- Energy conservation awareness
- Sustainable product choices

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- Original Android Carbon Footprint application for inspiration
- Bootstrap team for the excellent UI framework
- Chart.js for interactive data visualization
- Font Awesome for beautiful icons

## Support

If you encounter any issues or have questions, please:
1. Check the existing issues in the repository
2. Create a new issue with detailed information
3. Contact the development team

---

**Make a difference today! Start tracking your carbon footprint and join the movement towards a more sustainable future.** 🌱