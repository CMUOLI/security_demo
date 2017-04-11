from django.http import HttpResponse
from django.contrib.auth.decorators import login_required, permission_required
from django.shortcuts import render

def index(request):
    return HttpResponse('Please <a href="/secureme">Login</a>!')

@login_required
# @permission_required('myapp.can_login', raise_exception=True)
def secureme(request):
    return render(request, 'myapp/oidc_login.html')
