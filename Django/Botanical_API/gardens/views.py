import django_filters
from django.http import Http404
from rest_framework import filters
from rest_framework import status
from rest_framework.generics import ListAPIView
from rest_framework.response import Response
from rest_framework.views import APIView

from .models import Collection
from .models import Species
from .models import SpeciesInstance
from .serializers import CollectionSerializer
from .serializers import SpeciesInstanceSerializer
from .serializers import SpeciesSerializer


class SpeciesList(APIView):
	def get(self, request):
		species = Species.objects.all()
		serializer = SpeciesSerializer(species, many=True)
		return Response(serializer.data)

	def post(self, request):
		serializer = SpeciesSerializer(data=request.data)
		if serializer.is_valid():
			serializer.save()
			return Response(serializer.data, status=status.HTTP_201_CREATED)
		return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)


class CollectionList(APIView):
	def get(self, request):
		collections = Collection.objects.all()
		serializer = CollectionSerializer(collections, many=True)
		return Response(serializer.data)

	def post(self, request):
		serializer = CollectionSerializer(data=request.data)
		if serializer.is_valid():
			serializer.save()
			return Response(serializer.data, status=status.HTTP_201_CREATED)
		return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)


class SpeciesInstanceFilter(django_filters.rest_framework.FilterSet):
	desc = django_filters.CharFilter(name="siteSpecificDescription", lookup_expr='contains')
	collection = django_filters.CharFilter(name="collectionID__collectionName", lookup_expr='contains')
	genName = django_filters.CharFilter(name="speciesID__genName")
	sciName = django_filters.CharFilter(name="speciesID__sciName")
	class Meta:
		model = SpeciesInstance
		fields = ['longitude', 'latitude', 'desc', 'collection', 'genName', 'sciName',  "siteSpecificImgResID", "dateCreated", "dateModified", "speciesID"]


class SpeciesInstanceList(ListAPIView):
	queryset = SpeciesInstance.objects.all()
	serializer_class = SpeciesInstanceSerializer
	filter_class = SpeciesInstanceFilter



class SearchItems(ListAPIView):
    queryset = SpeciesInstance.objects.all()
    serializer_class = SpeciesInstanceSerializer
    filter_backends = (filters.SearchFilter,)
    search_fields = ('collectionID__collectionName', 'collectionID__collectionDescription', 'siteSpecificDescription', 'speciesID__genName', 'speciesID__sciName', 'speciesID__genDescription')






class SpeciesDetail(APIView):

	def get_object(self, pk):
		try:
			return Species.objects.get(pk=pk)
		except Species.DoesNotExist:
			raise Http404

	def get(self, request, pk, format=None):
		species = self.get_object(pk)
		species = SpeciesSerializer(species)
		return Response(species.data)


class SpeciesInstanceDetail(APIView):

	def get_object(self, pk):
		try:
			return SpeciesInstance.objects.get(pk=pk)
		except SpeciesInstance.DoesNotExist:
			raise Http404

	def get(self, request, pk, format=None):
		speciesInstance = self.get_object(pk)
		speciesInstance = SpeciesInstanceSerializer(speciesInstance)
		return Response(speciesInstance.data)


class CollectionDetail(APIView):

	def get_object(self, pk):
		try:
			return SpeciesInstance.objects.get(pk=pk)
		except SpeciesInstance.DoesNotExist:
			raise Http404

	def get(self, request, pk, format=None):
		speciesInstance = self.get_object(pk = pk)
		speciesInstance = SpeciesInstanceSerializer(speciesInstance)
		return Response(speciesInstance.data)
