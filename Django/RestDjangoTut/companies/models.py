from django.db import models

class Stock(models.Model):
    ticker = models.CharField(max_length=10)
    open = models.FloatField()
    close = models.FloatField()
    volume = models.IntegerField()

    def __str__(self):
        return self.ticker


class Trade(models.Model):
    user = models.CharField(max_length=50)
    stock = models.ForeignKey(Stock, on_delete=models.CASCADE)

    def __str__(self):
        return self.user + ' - ' + self.stock.ticker
