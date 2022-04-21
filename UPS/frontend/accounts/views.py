from django.shortcuts import render, redirect
from .forms import NewUserForm, ChangeLocationForm
from django.contrib.auth import login
from django.contrib.auth.models import User
from django.contrib import messages
from .models import Package, Product
from django.contrib.auth.decorators import login_required
# Create your views here.

# function to check whether user is logged in


def register_request(request):
    if request.method == "POST":
        form = NewUserForm(request.POST)
        if form.is_valid():
            user = form.save()
            login(request, user)
            messages.success(request, "Registration successful.")
            return redirect("home")
        messages.error(
            request, "Unsuccessful registration. Invalid information.")
    form = NewUserForm()
    return render(request=request, template_name="registration/register.html", context={"register_form": form})


@login_required
def view_packages(request):
    current_user = User.objects.get(pk=request.user.id)
    try:
        packages = Package.objects.filter(user_name=current_user.username)
    except:
        return render(request, "home/error.html")

    return render(request, "home/packages.html", {"packages": list(packages)})


@login_required
def package(request, id):
    if request.method == "GET":
        pack = Package.objects.get(package_id=id)
        products = Product.objects.filter(package=pack)
        form = ChangeLocationForm()
        return render(request, "home/package.html", {"package": pack, "products": list(products), "form": form})

    form = ChangeLocationForm(request.POST)
    if form.is_valid():
        newx = form.cleaned_data['x']
        newy = form.cleaned_data['y']
        Package.objects.filter(package_id=id).update(x=newx, y=newy)
        return redirect("home")
