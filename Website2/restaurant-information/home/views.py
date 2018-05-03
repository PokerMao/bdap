from django.views.generic import TemplateView, ListView
from django.shortcuts import render, redirect
from home.forms import SearchForm
from .models import Restaurant

class HomeView(TemplateView):
    template_name = 'home/home.html'
    fields = ['longitude','latitude']
    longitude = 0.0
    latitude = 0.0

    def get(self,request):
        form = SearchForm()
        return render(request, self.template_name, {'form': form})

    def post(self,request):
        form = SearchForm(request.POST)
        # check whether it's valid:
        if form.is_valid():
            HomeView.longitude = form.cleaned_data['longitude']
            HomeView.latitude = form.cleaned_data['latitude']
            return redirect('result')

class ResultView(ListView):
    template_name = 'home/result.html'
    context_object_name = 'list'

    def get_queryset(self):
        tmp = Restaurant.objects.filter(longitude__range=[HomeView.longitude - 0.02, HomeView.longitude + 0.02])
        tmp2 = tmp.filter(latitude__range=[HomeView.latitude - 0.02, HomeView.latitude + 0.02])
        return tmp2

