from django.contrib import admin
from .models import Species
from .models import Collection
from .models import SpeciesInstance

admin.site.register(Species)
admin.site.register(Collection)
admin.site.register(SpeciesInstance)
