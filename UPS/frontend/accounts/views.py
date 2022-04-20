from django.shortcuts import render, redirect
from .forms import NewUserForm
from django.contrib.auth import login
from django.contrib import messages

# Create your views here.

#function to check whether user is logged in
def login_required(request):
   user = request.session.get('name', "None")

   if user == "None":
       return False
   
   return True




def register_request(request):
    if request.method == "POST":
        form = NewUserForm(request.POST)
        if form.is_valid():
            user = form.save()
            login(request, user)
           
            request.session['name'] = form.cleaned_data['username']

            messages.success(request, "Registration successful.")
            return redirect("login")
        messages.error(
            request, "Unsuccessful registration. Invalid information.")
    form = NewUserForm()
    return render(request=request, template_name="registration/register.html", context={"register_form": form})

def view_packages(request):
    if(login_required(request) == False):
        return

    packages = Package.objects.get(user_name = request.session['name'])
    return render(request, "home/packages.html", {"packages": packages})

def package(request, id):
    if(login_required(request) == False):
        return
    pack = Package.objects.get(package_id = id)
    products = Product.objects.get(package = pack)
    return render(request, "home/package.html", {"package": pack, "products": products})
