package com.ruchitech.quicklinkcaller.navhost

import com.ruchitech.quicklinkcaller.navhost.nav.MyRouteNavigator
import com.ruchitech.quicklinkcaller.navhost.nav.RouteNavigator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped


@Module
@InstallIn(ViewModelComponent::class)
class ViewModelModule {

    @Provides
    @ViewModelScoped
    fun bindRouteNavigator(): RouteNavigator = MyRouteNavigator()
}