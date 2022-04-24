from django import forms
from django.contrib.auth.forms import UserCreationForm
from django.contrib.auth.models import User
from .models import Package


class NewUserForm(UserCreationForm):
    email = forms.EmailField(required=True)

    class Meta:
        model = User
        fields = ("username", "email", "password1", "password2")

    def save(self, commit=True):
        user = super(NewUserForm, self).save(commit=False)
        user.email = self.cleaned_data['email']
        if commit:
            user.save()
        return user


class ChangeLocationForm(forms.ModelForm):
    x = forms.IntegerField()
    y = forms.IntegerField()

    class Meta:
        model = Package
        fields = ['x', 'y']

    def clean(self):
        cleaned_data = super().clean()
        x = cleaned_data.get("x")
        y = cleaned_data.get("y")
        if x < 1:
            raise forms.ValidationError(
                "The X coordinate should be greater than 0")
        if y < 1:
            raise forms.ValidationError(
                "The Y coordinate should be greater than 0")
