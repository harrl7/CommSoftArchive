from django.http import HttpResponse

from .models import Species, Collection, Item
from .serializers import SpeciesSerializer, CollectionSerializer, ItemSerializer
from rest_framework import mixins
from rest_framework import generics
from django_filters.rest_framework import DjangoFilterBackend
import django_filters
from rest_framework import filters

from django.http import Http404
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status

def index(request):
    return HttpResponse('Gardens Home')

#Can create a filter class which allows you to make more specific filters
class SpeciesFilter(django_filters.rest_framework.FilterSet):
	"""
	lookup_expr is a way to further filter input:
		None - a list of all lookup types will be generated
		exact - exactly what was specified
		iexact - ???
		not_exact - Is not equal to
		lt - lesser than
		gt - greater than
		gte - lesser than or equal to
		lte - greater than or equal to
		startswith - starts with
		endswith - ends with
		contains - contains
		not_contains - does not contain

	"""
	image_id = django_filters.NumberFilter(name="genImgResID", lookup_expr='exact')
	class Meta:
		model = Species
		fields = ['genName', 'sciName', 'genDescription', 'image_id']

class ItemFilter(django_filters.rest_framework.FilterSet):
    genName = django_filters.CharFilter(name="speciesID__genName")
    sciName = django_filters.CharFilter(name="speciesID__sciName")
    collectionName = django_filters.CharFilter(name="collectionID__collectionName")
    class Meta:
        model = Item
        fields = ['latitude', 'longitude', 'siteSpecificDescription', 'collectionID',  'genName', 'sciName', 'collectionName', 'speciesID']

class CollectionsFilter(django_filters.rest_framework.FilterSet):
    class Meta:
        model = Collection
        fields = ['collectionID', 'collectionName', 'collectionDescription']



#Below is the most concise idiomatic django way to write your serializer class
class SpeciesList(generics.ListCreateAPIView):
    queryset = Species.objects.all()
    serializer_class = SpeciesSerializer

    filter_backends = (django_filters.rest_framework.DjangoFilterBackend,)
    #filter_fields = ('genName', 'sciName')
    filter_class = SpeciesFilter

class SpeciesDetail(generics.RetrieveUpdateDestroyAPIView):
    queryset = Species.objects.all()
    serializer_class = SpeciesSerializer



class CollectionList(generics.ListCreateAPIView):
    queryset = Collection.objects.all()
    serializer_class = CollectionSerializer

    filter_backends = (django_filters.rest_framework.DjangoFilterBackend,)
    filter_class = CollectionsFilter

class CollectionDetail(generics.RetrieveUpdateDestroyAPIView):
    queryset = Collection.objects.all()
    serializer_class = CollectionSerializer



class ItemList(generics.ListCreateAPIView):
    queryset = Item.objects.all()
    serializer_class = ItemSerializer

    filter_backends = (django_filters.rest_framework.DjangoFilterBackend,)
    filter_class = ItemFilter

class ItemDetail(generics.RetrieveUpdateDestroyAPIView):
    queryset = Item.objects.all()
    serializer_class = ItemSerializer

class SearchItems(generics.ListCreateAPIView):
    queryset = Item.objects.all()
    serializer_class = ItemSerializer
    filter_backends = (filters.SearchFilter,)
    search_fields = ('collectionID__collectionName', 'collectionID__collectionDescription', 'siteSpecificDescription', 'speciesID__genName', 'speciesID__sciName', 'speciesID__genDescription')