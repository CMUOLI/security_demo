FROM python:3

COPY . /usr/src/kclexample
WORKDIR /usr/src/kclexample

RUN pip install git+https://github.com/jhuapl-boss/django-oidc.git
RUN pip install git+https://github.com/jhuapl-boss/drf-oidc-auth.git
RUN pip install git+https://github.com/jhuapl-boss/boss-oidc.git

RUN ["python", "manage.py", "makemigrations", "bossoidc"]
RUN ["python", "manage.py", "migrate"]
RUN ["python", "manage.py", "runserver"]

EXPOSE 8000
