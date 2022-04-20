from django.shortcuts import render, redirect
from django.views.generic.base import TemplateView
from django.contrib.auth.mixins import LoginRequiredMixin
from django.contrib.auth.decorators import login_required
from accounts.models import Package, Product
from django.http import HttpResponse, JsonResponse


def index(request):
    return render(request, 'index.html')


def results(request):
    packageid = request.GET.get("q", 0)
    if not packageid:
        return render(request, 'error.html')
    try:
        package_result = Package.objects.filter(package_id=packageid)
    except:
        return render(request, 'error.html')
    return render(request, 'search_results.html', {'package': package_result})


def package_detail(request):
    packageid = request.GET.get("id", 0)
    if not packageid:
        return render(request, 'error.html')
    products = Product.objects.filter(package_id=packageid)
    return render(request, "package_detail.html", {'products': list(products)})
