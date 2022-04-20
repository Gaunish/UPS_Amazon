from django.shortcuts import render, redirect
from django.views.generic.base import TemplateView
from django.contrib.auth.mixins import LoginRequiredMixin
from django.contrib.auth.decorators import login_required


def index(request):
    return render(request, 'index.html')
