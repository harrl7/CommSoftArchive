from rest_framework import serializers
from .models import Stock, Trade

class StockSerializer(serializers.ModelSerializer):

    class Meta:
        model = Stock
        #fields = ('ticker', 'volume')
        fields = '__all__'


class TradeSerializer(serializers.ModelSerializer):

    class Meta:
        model = Trade
        fields = '__all__'
