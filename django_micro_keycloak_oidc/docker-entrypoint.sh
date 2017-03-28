#!/bin/bash

# Apply migrations and add superuser
echo "Apply migrations"
python manage.py makemigrations bossoidc
python manage.py migrate
# if just using plain oidc_provider uncomment the below line and the bossoidc RUN layer above
# python manage.py creatersakey
python manage.py shell --plain < scripts/createsuperuser.py

# Start server
echo "Starting server"
python manage.py runserver 0.0.0.0:8000
