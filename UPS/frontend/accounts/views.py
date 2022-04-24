from django.shortcuts import render, redirect
from .forms import NewUserForm, ChangeLocationForm
from django.contrib.auth import login
from django.contrib.auth.models import User
from django.contrib import messages
from .models import Package, Product
from django.contrib.auth.decorators import login_required
from django.core.mail import send_mail, BadHeaderError
from django.http import HttpResponse
from django.contrib.auth.forms import PasswordResetForm
from django.template.loader import render_to_string
from django.db.models.query_utils import Q
from django.utils.http import urlsafe_base64_encode
from django.contrib.auth.tokens import default_token_generator
from django.utils.encoding import force_bytes
# Create your views here.

# function to check whether user is logged in


def password_reset_request(request):
    if request.method == "POST":
        password_reset_form = PasswordResetForm(request.POST)
        if password_reset_form.is_valid():
            data = password_reset_form.cleaned_data['email']
            associated_users = User.objects.filter(Q(email=data))
            if associated_users.exists():
                for user in associated_users:
                    subject = "Password Reset Requested"
                    email_template_name = "password/password_reset_email.txt"
                    c = {
                        "email": user.email,
                        'domain': '127.0.0.1:8000',
                        'site_name': 'Website',
                        "uid": urlsafe_base64_encode(force_bytes(user.pk)),
                        "user": user,
                        'token': default_token_generator.make_token(user),
                        'protocol': 'http',
                    }
                    email = render_to_string(email_template_name, c)
                    try:
                        send_mail(subject, email, 'admin@example.com',
                                  [user.email], fail_silently=False)
                    except BadHeaderError:
                        return HttpResponse('Invalid header found.')
                    return redirect("password_reset/done/")
    password_reset_form = PasswordResetForm()
    return render(request=request, template_name="password/password_reset.html", context={"password_reset_form": password_reset_form})


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
        history = History.objects.filter(package_id=id)
        form = ChangeLocationForm()
        return render(request, "home/package.html", {"package": pack, "products": list(products), "form": form, "history": history})

    form = ChangeLocationForm(request.POST)
    if form.is_valid():
        newx = form.cleaned_data['x']
        newy = form.cleaned_data['y']
        Package.objects.filter(package_id=id).update(x=newx, y=newy)
        return redirect("home")


@login_required
def profile(request):
    current_user = User.objects.get(pk=request.user.id)
    return render(request, "profile.html", {"user": current_user})
