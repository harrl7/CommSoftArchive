from django.db import models

class Collection(models.Model):
	collectionID = models.AutoField(primary_key=True)
	collectionName = models.CharField(max_length=50)
	collectionDescription = models.TextField(max_length=400)

	def __str__(self):
		return self.collectionName

class Species(models.Model):
	speciesID = models.AutoField(primary_key=True)
	genName = models.CharField(max_length=50)
	sciName = models.CharField(max_length=50)
	genDescription = models.TextField(max_length=400)
	genImgResID = models.IntegerField()

	def __str__(self):
		return self.genName

class SpeciesInstance(models.Model):
	speciesIntanceID = models.AutoField(primary_key=True)
	speciesID = models.ForeignKey(Species, on_delete=models.CASCADE)
	collectionID = models.ForeignKey(Collection, on_delete=models.CASCADE)
	latitude = models.FloatField()
	longitude = models.FloatField()
	siteSpecificDescription = models.TextField(max_length=400)
	siteSpecificImgResID = models.IntegerField()
	dateCreated = models.DateField(auto_now_add=False)
	dateModified = models.DateField(auto_now=False)

	def __str__(self):
		return self.siteSpecificDescription