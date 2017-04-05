from django.http import HttpResponse
from django.contrib.auth.decorators import login_required

def index(request):
    return HttpResponse('Please <a href="/secureme">Login</a>!')

@login_required
def secureme(request):
    return HttpResponse('You\'ve logged in! Now <a href="/openid/logout">Logout</a>')
