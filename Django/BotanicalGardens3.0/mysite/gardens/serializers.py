from rest_framework import serializers
from .models import Species
from .models import Collection
from .models import Item

class SpeciesSerializer(serializers.ModelSerializer):
	class Meta:
		model = Species
		#fields = ('genName', 'sciName')
		fields = '__all__'

class CollectionSerializer(serializers.ModelSerializer):
	class Meta:
		model = Collection
		fields = '__all__'

class ItemSerializer(serializers.ModelSerializer):
	class Meta:
		model = Item
		fields = '__all__'
		depth = 1

class ItemSerializer2(serializers.ModelSerializer):
	species = serializers.StringRelatedField(many=False)
	class Meta:
		model: Item

