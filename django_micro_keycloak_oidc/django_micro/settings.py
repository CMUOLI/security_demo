"""
Django settings for django_micro project.

Generated by 'django-admin startproject' using Django 1.9.7.

For more information on this file, see
https://docs.djangoproject.com/en/1.9/topics/settings/

For the full list of settings and their values, see
https://docs.djangoproject.com/en/1.9/ref/settings/
"""

import os

# Build paths inside the project like this: os.path.join(BASE_DIR, ...)
BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))


# Quick-start development settings - unsuitable for production
# See https://docs.djangoproject.com/en/1.9/howto/deployment/checklist/

# SECURITY WARNING: keep the secret key used in production secret!
SECRET_KEY = 'u@m5rns8^pgfjua&hn-ph(#e)n!$%tmn47ab*%c1^bs+-hc*xr'

# SECURITY WARNING: don't run with debug turned on in production!
DEBUG = True

ALLOWED_HOSTS = ["*"]


# Application definition

INSTALLED_APPS = [
    'django.contrib.admin',
    'django.contrib.auth',
    'django.contrib.contenttypes',
    'django.contrib.sessions',
    'django.contrib.messages',
    'django.contrib.staticfiles',
    'bossoidc',
    'djangooidc',
    'myapp',
]

AUTHENTICATION_BACKENDS = (
    'django.contrib.auth.backends.ModelBackend',
    'bossoidc.backend.OpenIdConnectBackend',
)

auth_uri = "https://stormwind.oli.cmu.edu/auth/realms/tpz_prod"
client_id = "tpz"
public_uri = "http://localhost:8083/"

from bossoidc.settings import *
configure_oidc(auth_uri, client_id, public_uri)

OIDC_DYNAMIC_CLIENT_REGISTRATION_DATA = {
    "application_type": "web",
    "contacts": ["onlinecloudcomputingcourse@gmail.com"],
    "redirect_uris": ["http://localhost:8083/openid/callback/login"],
    "post_logout_redirect_uris": ["http://localhost:8083/openid/callback/logout"]
}

OIDC_DEFAULT_BEHAVIOUR = {
    "response_type": "code",
    "scope": ["openid", "profile", "email", "address", "phone"],
}

OIDC_PROVIDERS = {
    "KeyCloak": {
        "srv_discovery_url": "https://stormwind.oli.cmu.edu/auth/realms/tpz_prod",
        "behaviour": OIDC_DEFAULT_BEHAVIOUR,
        "client_registration": {
            "client_id": "tpz",
            "client_secret": os.environ['client_secret'],
            "redirect_uris": ["http://localhost:8083/openid/callback/login"],
            "post_logout_redirect_uris": ["http://localhost:8083/openid/callback/logout"],
        }
    }
}

OIDC_ALLOW_DYNAMIC_OP = True
OIDC_SESSION_MANAGEMENT_ENABLE = True

MIDDLEWARE_CLASSES = [
    'django.middleware.security.SecurityMiddleware',
    'django.contrib.sessions.middleware.SessionMiddleware',
    'django.middleware.common.CommonMiddleware',
    'django.middleware.csrf.CsrfViewMiddleware',
    'django.contrib.auth.middleware.AuthenticationMiddleware',
    'django.contrib.auth.middleware.SessionAuthenticationMiddleware',
    'django.contrib.messages.middleware.MessageMiddleware',
    'django.middleware.clickjacking.XFrameOptionsMiddleware',
]

ROOT_URLCONF = 'django_micro.urls'

TEMPLATES = [
    {
        'BACKEND': 'django.template.backends.django.DjangoTemplates',
        'DIRS': [],
        'APP_DIRS': True,
        'OPTIONS': {
            'context_processors': [
                'django.template.context_processors.debug',
                'django.template.context_processors.request',
                'django.contrib.auth.context_processors.auth',
                'django.contrib.messages.context_processors.messages',
            ],
        },
    },
]

WSGI_APPLICATION = 'django_micro.wsgi.application'


# Database
# https://docs.djangoproject.com/en/1.9/ref/settings/#databases

DATABASES = {
    'default': {
        'ENGINE': 'django.db.backends.sqlite3',
        'NAME': os.path.join(BASE_DIR, 'db.sqlite3'),
    }
}


# Password validation
# https://docs.djangoproject.com/en/1.9/ref/settings/#auth-password-validators

AUTH_PASSWORD_VALIDATORS = [
    {
        'NAME': 'django.contrib.auth.password_validation.UserAttributeSimilarityValidator',
    },
    {
        'NAME': 'django.contrib.auth.password_validation.MinimumLengthValidator',
    },
    {
        'NAME': 'django.contrib.auth.password_validation.CommonPasswordValidator',
    },
    {
        'NAME': 'django.contrib.auth.password_validation.NumericPasswordValidator',
    },
]


# Internationalization
# https://docs.djangoproject.com/en/1.9/topics/i18n/

LANGUAGE_CODE = 'en-us'

TIME_ZONE = 'UTC'

USE_I18N = True

USE_L10N = True

USE_TZ = True


# Static files (CSS, JavaScript, Images)
# https://docs.djangoproject.com/en/1.9/howto/static-files/

STATIC_URL = '/static/'
