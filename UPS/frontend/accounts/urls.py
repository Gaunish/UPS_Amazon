from django.contrib.auth import views
from django.urls import path
from . import views as custom_views

urlpatterns = [
    path('login/', views.LoginView.as_view(), name='login'),
    path('logout/', views.LogoutView.as_view(template_name='registration/logged_out.html'), name='logout'),
    path("register/", custom_views.register_request, name="register"),
    path('password_reset/done/', views.PasswordResetDoneView.as_view(
        template_name='password/password_reset_done.html'), name='password_reset_done'),
    path('reset/<uidb64>/<token>/', views.PasswordResetConfirmView.as_view(
        template_name="password/password_reset_confirm.html"), name='password_reset_confirm'),
    path('reset/done/', views.PasswordResetCompleteView.as_view(
        template_name='password/password_reset_complete.html'), name='password_reset_complete'),
    path("password_reset", custom_views.password_reset_request,
         name="password_reset"),
    path('package/<int:id>', custom_views.package, name="package"),
    path('profile', custom_views.profile, name="profile"),
]
