import jwt, requests, os

from django.http import HttpResponse
from django.contrib.auth.decorators import login_required, permission_required
from django.shortcuts import render

def index(request):
    return HttpResponse('Please <a href="/secureme">Login</a>!')

@login_required
def secureme(request):
    url = "https://stormwind.oli.cmu.edu/auth/realms/tpz_prod/protocol/openid-connect/token"
    headers = {'Content-Type': 'application/x-www-form-urlencoded'}
    data = {'username': 'acorlett', 'password': os.environ['password'], 'grant_type': 'password', 'client_id': 'tpz', 'client_secret': os.environ['client_secret']}
    result = requests.post(url, headers=headers, data=data)
    access_token = result.json()["access_token"]
    try:
        # remove verify=False when running decode behind https
        user_data = jwt.decode(access_token, os.environ['client_secret'], issuer="https://stormwind.oli.cmu.edu/auth/realms/tpz_prod", verify=False)
    except jwt.InvalidTokenError:
        return 401  # Invalid token
    except jwt.ExpiredSignatureError:
        return 401  # Token has expired
    except jwt.InvalidIssuerError:
        return 401  # Token is not issued by Google
    except jwt.InvalidAudienceError:
        return 401  # Token is not valid for this endpoint
    # return render(request, 'myapp/oidc_login.html', {"decoded" : access_token})
    return render(request, 'myapp/oidc_login.html', {"decoded" : user_data})
