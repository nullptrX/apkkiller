.class public Lio/github/nullptrx/App;
.super Landroid/app/Application;
.source "App.java"

# interfaces
.implements Ljava/lang/reflect/InvocationHandler;


# static fields
.field private static final GET_SIGNATURES:I = 0x40


# instance fields
.field private appPkgName:Ljava/lang/String;

.field private base:Ljava/lang/Object;

.field private sign:[[B


# direct methods
.method public constructor <init>()V
    .registers 2

    .prologue
    .line 22
    invoke-direct {p0}, Landroid/app/Application;-><init>()V

    .line 27
    const-string v0, ""

    iput-object v0, p0, Lio/github/nullptrx/App;->appPkgName:Ljava/lang/String;

    return-void
.end method

.method private hook(Landroid/content/Context;)V
    .registers 22
    .param p1, "context"    # Landroid/content/Context;

    .prologue
    .line 73
    :try_start_0
    const-string v6, "### Signatures Data ###"

    .line 74
    .local v6, "data":Ljava/lang/String;
    new-instance v10, Ljava/io/DataInputStream;

    new-instance v17, Ljava/io/ByteArrayInputStream;

    const/16 v18, 0x0

    move/from16 v0, v18

    invoke-static {v6, v0}, Landroid/util/Base64;->decode(Ljava/lang/String;I)[B

    move-result-object v18

    invoke-direct/range {v17 .. v18}, Ljava/io/ByteArrayInputStream;-><init>([B)V

    move-object/from16 v0, v17

    invoke-direct {v10, v0}, Ljava/io/DataInputStream;-><init>(Ljava/io/InputStream;)V

    .line 75
    .local v10, "is":Ljava/io/DataInputStream;
    invoke-virtual {v10}, Ljava/io/DataInputStream;->read()I

    move-result v17

    move/from16 v0, v17

    and-int/lit16 v0, v0, 0xff

    move/from16 v17, v0

    move/from16 v0, v17

    new-array v0, v0, [[B

    move-object/from16 v16, v0

    .line 76
    .local v16, "sign":[[B
    const/4 v8, 0x0

    .local v8, "i":I
    :goto_27
    move-object/from16 v0, v16

    array-length v0, v0

    move/from16 v17, v0

    move/from16 v0, v17

    if-ge v8, v0, :cond_46

    .line 77
    invoke-virtual {v10}, Ljava/io/DataInputStream;->readInt()I

    move-result v17

    move/from16 v0, v17

    new-array v0, v0, [B

    move-object/from16 v17, v0

    aput-object v17, v16, v8

    .line 78
    aget-object v17, v16, v8

    move-object/from16 v0, v17

    invoke-virtual {v10, v0}, Ljava/io/DataInputStream;->readFully([B)V

    .line 76
    add-int/lit8 v8, v8, 0x1

    goto :goto_27

    .line 82
    :cond_46
    const-string v17, "android.app.ActivityThread"

    invoke-static/range {v17 .. v17}, Ljava/lang/Class;->forName(Ljava/lang/String;)Ljava/lang/Class;

    move-result-object v3

    .line 83
    .local v3, "activityThreadClass":Ljava/lang/Class;, "Ljava/lang/Class<*>;"
    const-string v17, "currentActivityThread"

    const/16 v18, 0x0

    move/from16 v0, v18

    new-array v0, v0, [Ljava/lang/Class;

    move-object/from16 v18, v0

    .line 84
    move-object/from16 v0, v17

    move-object/from16 v1, v18

    invoke-virtual {v3, v0, v1}, Ljava/lang/Class;->getDeclaredMethod(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;

    move-result-object v5

    .line 85
    .local v5, "currentActivityThreadMethod":Ljava/lang/reflect/Method;
    const/16 v17, 0x0

    const/16 v18, 0x0

    move/from16 v0, v18

    new-array v0, v0, [Ljava/lang/Object;

    move-object/from16 v18, v0

    move-object/from16 v0, v17

    move-object/from16 v1, v18

    invoke-virtual {v5, v0, v1}, Ljava/lang/reflect/Method;->invoke(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;

    move-result-object v4

    .line 88
    .local v4, "currentActivityThread":Ljava/lang/Object;
    const-string v17, "sPackageManager"

    move-object/from16 v0, v17

    invoke-virtual {v3, v0}, Ljava/lang/Class;->getDeclaredField(Ljava/lang/String;)Ljava/lang/reflect/Field;

    move-result-object v15

    .line 89
    .local v15, "sPackageManagerField":Ljava/lang/reflect/Field;
    const/16 v17, 0x1

    move/from16 v0, v17

    invoke-virtual {v15, v0}, Ljava/lang/reflect/Field;->setAccessible(Z)V

    .line 90
    invoke-virtual {v15, v4}, Ljava/lang/reflect/Field;->get(Ljava/lang/Object;)Ljava/lang/Object;

    move-result-object v14

    .line 93
    .local v14, "sPackageManager":Ljava/lang/Object;
    const-string v17, "android.content.pm.IPackageManager"

    invoke-static/range {v17 .. v17}, Ljava/lang/Class;->forName(Ljava/lang/String;)Ljava/lang/Class;

    move-result-object v9

    .line 94
    .local v9, "iPackageManagerInterface":Ljava/lang/Class;, "Ljava/lang/Class<*>;"
    move-object/from16 v0, p0

    iput-object v14, v0, Lio/github/nullptrx/App;->base:Ljava/lang/Object;

    .line 95
    move-object/from16 v0, v16

    move-object/from16 v1, p0

    iput-object v0, v1, Lio/github/nullptrx/App;->sign:[[B

    .line 96
    invoke-virtual/range {p1 .. p1}, Landroid/content/Context;->getPackageName()Ljava/lang/String;

    move-result-object v17

    move-object/from16 v0, v17

    move-object/from16 v1, p0

    iput-object v0, v1, Lio/github/nullptrx/App;->appPkgName:Ljava/lang/String;

    .line 99
    invoke-virtual {v9}, Ljava/lang/Class;->getClassLoader()Ljava/lang/ClassLoader;

    move-result-object v17

    const/16 v18, 0x1

    move/from16 v0, v18

    new-array v0, v0, [Ljava/lang/Class;

    move-object/from16 v18, v0

    const/16 v19, 0x0

    aput-object v9, v18, v19

    .line 98
    move-object/from16 v0, v17

    move-object/from16 v1, v18

    move-object/from16 v2, p0

    invoke-static {v0, v1, v2}, Ljava/lang/reflect/Proxy;->newProxyInstance(Ljava/lang/ClassLoader;[Ljava/lang/Class;Ljava/lang/reflect/InvocationHandler;)Ljava/lang/Object;

    move-result-object v13

    .line 104
    .local v13, "proxy":Ljava/lang/Object;
    invoke-virtual {v15, v4, v13}, Ljava/lang/reflect/Field;->set(Ljava/lang/Object;Ljava/lang/Object;)V

    .line 107
    invoke-virtual/range {p1 .. p1}, Landroid/content/Context;->getPackageManager()Landroid/content/pm/PackageManager;

    move-result-object v12

    .line 108
    .local v12, "pm":Landroid/content/pm/PackageManager;
    invoke-virtual {v12}, Ljava/lang/Object;->getClass()Ljava/lang/Class;

    move-result-object v17

    const-string v18, "mPM"

    invoke-virtual/range {v17 .. v18}, Ljava/lang/Class;->getDeclaredField(Ljava/lang/String;)Ljava/lang/reflect/Field;

    move-result-object v11

    .line 109
    .local v11, "mPmField":Ljava/lang/reflect/Field;
    const/16 v17, 0x1

    move/from16 v0, v17

    invoke-virtual {v11, v0}, Ljava/lang/reflect/Field;->setAccessible(Z)V

    .line 110
    invoke-virtual {v11, v12, v13}, Ljava/lang/reflect/Field;->set(Ljava/lang/Object;Ljava/lang/Object;)V

    .line 111
    sget-object v17, Ljava/lang/System;->out:Ljava/io/PrintStream;

    const-string v18, "PmsHook success."

    invoke-virtual/range {v17 .. v18}, Ljava/io/PrintStream;->println(Ljava/lang/String;)V
    :try_end_d9
    .catch Ljava/lang/Exception; {:try_start_0 .. :try_end_d9} :catch_da

    .line 116
    .end local v3    # "activityThreadClass":Ljava/lang/Class;, "Ljava/lang/Class<*>;"
    .end local v4    # "currentActivityThread":Ljava/lang/Object;
    .end local v5    # "currentActivityThreadMethod":Ljava/lang/reflect/Method;
    .end local v6    # "data":Ljava/lang/String;
    .end local v8    # "i":I
    .end local v9    # "iPackageManagerInterface":Ljava/lang/Class;, "Ljava/lang/Class<*>;"
    .end local v10    # "is":Ljava/io/DataInputStream;
    .end local v11    # "mPmField":Ljava/lang/reflect/Field;
    .end local v12    # "pm":Landroid/content/pm/PackageManager;
    .end local v13    # "proxy":Ljava/lang/Object;
    .end local v14    # "sPackageManager":Ljava/lang/Object;
    .end local v15    # "sPackageManagerField":Ljava/lang/reflect/Field;
    .end local v16    # "sign":[[B
    :goto_d9
    return-void

    .line 112
    :catch_da
    move-exception v7

    .line 113
    .local v7, "e":Ljava/lang/Exception;
    sget-object v17, Ljava/lang/System;->err:Ljava/io/PrintStream;

    const-string v18, "PmsHook failed."

    invoke-virtual/range {v17 .. v18}, Ljava/io/PrintStream;->println(Ljava/lang/String;)V

    .line 114
    invoke-virtual {v7}, Ljava/lang/Exception;->printStackTrace()V

    goto :goto_d9
.end method


# virtual methods
.method protected attachBaseContext(Landroid/content/Context;)V
    .registers 2
    .param p1, "base"    # Landroid/content/Context;

    .prologue
    .line 31
    invoke-direct {p0, p1}, Lio/github/nullptrx/App;->hook(Landroid/content/Context;)V

    .line 32
    invoke-super {p0, p1}, Landroid/app/Application;->attachBaseContext(Landroid/content/Context;)V

    .line 33
    return-void
.end method

.method public getApplicationInfo()Landroid/content/pm/ApplicationInfo;
    .registers 3

    .prologue
    .line 45
    invoke-super {p0}, Landroid/app/Application;->getApplicationInfo()Landroid/content/pm/ApplicationInfo;

    move-result-object v0

    .line 50
    .local v0, "info":Landroid/content/pm/ApplicationInfo;
    const/4 v1, 0x0

    iput v1, v0, Landroid/content/pm/ApplicationInfo;->flags:I

    .line 51
    return-object v0
.end method

.method public invoke(Ljava/lang/Object;Ljava/lang/reflect/Method;[Ljava/lang/Object;)Ljava/lang/Object;
    .registers 11
    .param p1, "proxy"    # Ljava/lang/Object;
    .param p2, "method"    # Ljava/lang/reflect/Method;
    .param p3, "args"    # [Ljava/lang/Object;
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Ljava/lang/Throwable;
        }
    .end annotation

    .prologue
    .line 56
    const-string v4, "getPackageInfo"

    invoke-virtual {p2}, Ljava/lang/reflect/Method;->getName()Ljava/lang/String;

    move-result-object v5

    invoke-virtual {v4, v5}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v4

    if-eqz v4, :cond_4b

    .line 57
    const/4 v4, 0x0

    aget-object v3, p3, v4

    check-cast v3, Ljava/lang/String;

    .line 58
    .local v3, "pkgName":Ljava/lang/String;
    const/4 v4, 0x1

    aget-object v0, p3, v4

    check-cast v0, Ljava/lang/Integer;

    .line 59
    .local v0, "flag":Ljava/lang/Integer;
    invoke-virtual {v0}, Ljava/lang/Integer;->intValue()I

    move-result v4

    and-int/lit8 v4, v4, 0x40

    if-eqz v4, :cond_4b

    iget-object v4, p0, Lio/github/nullptrx/App;->appPkgName:Ljava/lang/String;

    invoke-virtual {v4, v3}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v4

    if-eqz v4, :cond_4b

    .line 60
    iget-object v4, p0, Lio/github/nullptrx/App;->base:Ljava/lang/Object;

    invoke-virtual {p2, v4, p3}, Ljava/lang/reflect/Method;->invoke(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;

    move-result-object v2

    check-cast v2, Landroid/content/pm/PackageInfo;

    .line 61
    .local v2, "info":Landroid/content/pm/PackageInfo;
    iget-object v4, p0, Lio/github/nullptrx/App;->sign:[[B

    array-length v4, v4

    new-array v4, v4, [Landroid/content/pm/Signature;

    iput-object v4, v2, Landroid/content/pm/PackageInfo;->signatures:[Landroid/content/pm/Signature;

    .line 62
    const/4 v1, 0x0

    .local v1, "i":I
    :goto_36
    iget-object v4, v2, Landroid/content/pm/PackageInfo;->signatures:[Landroid/content/pm/Signature;

    array-length v4, v4

    if-ge v1, v4, :cond_51

    .line 63
    iget-object v4, v2, Landroid/content/pm/PackageInfo;->signatures:[Landroid/content/pm/Signature;

    new-instance v5, Landroid/content/pm/Signature;

    iget-object v6, p0, Lio/github/nullptrx/App;->sign:[[B

    aget-object v6, v6, v1

    invoke-direct {v5, v6}, Landroid/content/pm/Signature;-><init>([B)V

    aput-object v5, v4, v1

    .line 62
    add-int/lit8 v1, v1, 0x1

    goto :goto_36

    .line 68
    .end local v0    # "flag":Ljava/lang/Integer;
    .end local v1    # "i":I
    .end local v2    # "info":Landroid/content/pm/PackageInfo;
    .end local v3    # "pkgName":Ljava/lang/String;
    :cond_4b
    iget-object v4, p0, Lio/github/nullptrx/App;->base:Ljava/lang/Object;

    invoke-virtual {p2, v4, p3}, Ljava/lang/reflect/Method;->invoke(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;

    move-result-object v2

    :cond_51
    return-object v2
.end method

.method public onCreate()V
    .registers 1

    .prologue
    .line 37
    invoke-super {p0}, Landroid/app/Application;->onCreate()V

    .line 41
    return-void
.end method
