# This is an auto-generated Django model module.
# You'll have to do the following manually to clean this up:
#   * Rearrange models' order
#   * Make sure each model has one field with primary_key=True
#   * Make sure each ForeignKey and OneToOneField has `on_delete` set to the desired behavior
#   * Remove `managed = False` lines if you wish to allow Django to create, modify, and delete the table
# Feel free to rename the models, but don't rename db_table values or field names.
from django.db import models


class Package(models.Model):
    package_id = models.BigIntegerField(primary_key=True)
    x = models.IntegerField()
    y = models.IntegerField()
    truck_id = models.IntegerField()
    user_name = models.CharField(max_length=100)
    status = models.CharField(max_length=25)

    class Meta:
        managed = True
        db_table = 'package'


class Product(models.Model):
    product_id = models.AutoField(primary_key=True)
    package = models.ForeignKey(Package, models.DO_NOTHING)
    description = models.CharField(max_length=200, blank=True, null=True)
    count = models.IntegerField()

    class Meta:
        managed = False
        db_table = 'product'


class Package(models.Model):
    package_id = models.BigIntegerField(primary_key=True)
    x = models.IntegerField()
    y = models.IntegerField()
    truck_id = models.IntegerField()
    user_name = models.CharField(max_length=100)
    status = models.CharField(max_length=25)

    class Meta:
        managed = False
<<<<<<< HEAD
        db_table = 'users'
=======
        db_table = 'package'


class Product(models.Model):
    product_id = models.AutoField(primary_key=True)
    package = models.ForeignKey(Package, models.DO_NOTHING)
    description = models.CharField(max_length=200, blank=True, null=True)
    count = models.IntegerField()

    class Meta:
        managed = False
        db_table = 'product'


class Truck(models.Model):
    truck_id = models.IntegerField(primary_key=True)
    whid = models.IntegerField()
    status = models.CharField(max_length=20)
    x = models.IntegerField()
    y = models.IntegerField()

    class Meta:
        managed = False
        db_table = 'truck'
>>>>>>> cd8bf8b05200e73e838a8d0e51a9a4549bced70a
