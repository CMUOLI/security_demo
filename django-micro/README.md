To build locally:

```sh
# switch to python 3, pyenv can come in handy here

pip install git+https://github.com/jhuapl-boss/django-oidc.git
pip install git+https://github.com/jhuapl-boss/drf-oidc-auth.git
pip install git+https://github.com/jhuapl-boss/boss-oidc.git

python manage.py makemigrations bossoidc
python manage.py migrate
python manage.py runserver

# visit localhost:8000

```
