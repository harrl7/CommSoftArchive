from django.conf.urls import url
from . import views

urlpatterns = [

	# index
	url(r'^$', views.index, name='index'),

	# List
	url(r'^collection/$', views.CollectionList.as_view()),
	url(r'^species/$', views.SpeciesList.as_view()),
	url(r'^item/$', views.ItemList.as_view()),

	# Details
	url(r'^collection/(?P<pk>[0-9]+)/$', views.CollectionDetail.as_view()),
	url(r'^species/(?P<pk>[0-9]+)/$', views.SpeciesDetail.as_view()),
	url(r'^item/(?P<pk>[0-9]+)/$', views.ItemDetail.as_view()),

	# Search
	url(r'^item/search/$', views.SearchItems.as_view())
]
