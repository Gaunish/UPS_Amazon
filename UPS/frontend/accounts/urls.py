from django.contrib.auth import views
from django.urls import path
from . import views as custom_views

urlpatterns = [
    path('login/', views.LoginView.as_view(), name='login'),
    path('logout/', views.LogoutView.as_view(), name='logout'),
    path("register/", custom_views.register_request, name="register"),
    path('password-reset/', views.PasswordResetView.as_view(),
         name='password_reset'),
    path('password-change/done/', views.PasswordChangeDoneView.as_view(),
         name='password_change_done'),
    path('password-change/', views.PasswordChangeView.as_view(),
         name='password_change'),
    path('password-reset/done/', views.PasswordResetDoneView.as_view(),
         name='password_reset_done'),
]
