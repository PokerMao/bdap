from django.db import models

# Create your models here.

class Restaurant(models.Model):
    reviews = models.IntegerField()
    rating = models.DecimalField(max_digits=2, decimal_places=1)
    price = models.IntegerField()
    name = models.CharField(max_length=100)
    picture = models.CharField(max_length=10000)
    noise = models.IntegerField()
    grade = models.CharField(max_length=2)
    latitude = models.FloatField()
    longitude = models.FloatField()


    def __str__(self):
        return self.name