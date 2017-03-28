from django.conf.urls import url
from . import views

urlpatterns = [
    url(r'^$', views.index, name='index'),
    url(r'^secure$', views.secure, name='secure'),
]

# from django.contrib.auth.decorators import login_required
# from django.contrib.auth import views as auth_views
# from django.conf.urls import include, url
# from django.contrib import admin
# from django.views.generic import TemplateView

# urlpatterns = [
#     url(r'^$', TemplateView.as_view(template_name='home.html'), name='home'),
#     url(r'^accounts/login/$', auth_views.login, { 'template_name': 'login.html' }, name='login'),
#     url(r'^accounts/logout/$', auth_views.logout, { 'next_page': '/' }, name='logout'),
#     url(r'^secure$', views.secure, name='secure'),
# ]
