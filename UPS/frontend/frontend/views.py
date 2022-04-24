from django.shortcuts import render, redirect
from django.views.generic.base import TemplateView
from django.contrib.auth.mixins import LoginRequiredMixin
from django.contrib.auth.decorators import login_required
from accounts.models import Package, Product, Truck, History
from django.http import HttpResponse, JsonResponse
from plotly.offline import plot
import plotly.graph_objects as go
import numpy as np


def index(request):
    return render(request, "index.html")


def search(request):
    return render(request, 'search.html')


def results(request):
    packageid = request.GET.get("q", 0)
    if not packageid:
        return render(request, 'error.html')
    try:
        package_result = Package.objects.get(package_id=packageid)
    except:
        return render(request, 'error.html')
    return render(request, 'search_results.html', {'package': package_result})


def package_detail(request):
    packageid = request.GET.get("id", 0)
    if not packageid:
        return render(request, 'error.html')
    package_result = Package.objects.get(package_id=packageid)
    truck_result = Truck.objects.get(truck_id=package_result.truck_id)
    plot_div = plot({"data": [go.Scatter(x=[truck_result.x], y=[truck_result.y],
                                         mode='markers+text', text='Package', textposition='top center', name='Package', marker_color='green'), go.Scatter(x=[package_result.x], y=[package_result.y],
                                                                                                                                                           mode='markers+text', text='Destination', name='Destination', textposition='top center', marker_color='red')],
                     "layout": go.Layout(yaxis_range=[0, 10], xaxis_range=[0, 10],
                                         width=600, height=600,
                                         template="ggplot2")},
                    output_type='div')
    products = Product.objects.filter(package_id=packageid)
    estimate = (truck_result.x-package_result.x)**2 + \
        (truck_result.y-package_result.y)**2
    
    history = History.objects.filter(package_id=packageid)
    return render(request, "package_detail.html", {'products': list(products), 'plot_div': plot_div, 'estimate': estimate, "history": history})
