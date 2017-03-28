from django.contrib.auth.models import User
from django.db.utils import IntegrityError
import logging

logger = logging.getLogger(__name__)

class AdminCreate(object):
  def __init__(self):
    try:
      User.objects.create_superuser(username='admin', password='admin', email='')
    except IntegrityError as e:
      logger.warning("DB Error Thrown %s" % e)

create_super_user = AdminCreate()
