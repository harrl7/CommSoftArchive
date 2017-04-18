from rest_framework import serializers
from .models import Species
from .models import Collection
from .models import SpeciesInstance

class SpeciesSerializer(serializers.ModelSerializer):
	class Meta:
		model = Species
		#fields = ('genName', 'sciName')
		fields = '__all__'

class CollectionSerializer(serializers.ModelSerializer):
	class Meta:
		model = Collection
		fields = '__all__'

class SpeciesInstanceSerializer(serializers.ModelSerializer):
	class Meta:
		model = SpeciesInstance
		fields = '__all__'
