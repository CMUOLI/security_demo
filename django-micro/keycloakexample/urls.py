from django.conf.urls import url, include  
from django.contrib import admin

urlpatterns = [  
    url(r'', include('demo.urls')),
    url(r'^admin/', admin.site.urls),
    url(r'openid/', include('djangooidc.urls')),
]
