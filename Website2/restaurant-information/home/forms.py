from django import forms

class SearchForm(forms.Form):
    longitude = forms.FloatField()
    latitude = forms.FloatField()


