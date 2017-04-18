from django.contrib import admin
from .models import Species
from .models import Collection
from .models import Item

admin.site.register(Species)
admin.site.register(Collection)
admin.site.register(Item)
