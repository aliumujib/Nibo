package com.alium.nibo.di;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;

import com.alium.nibo.origindestinationpicker.OriginDestinationContracts;
import com.alium.nibo.placepicker.NiboPickerContracts;
import com.alium.nibo.placepicker.NiboPickerPresenter;
import com.alium.nibo.repo.contracts.IGeoCodingRepository;
import com.alium.nibo.repo.contracts.ISuggestionRepository;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;

/**
 * Created by aliumujib on 03/05/2018.
 */

public class Injection {


    private RepositoryModule repositoryModule;
    private GoogleClientModule googleClientModule;
    private ProviderModule providerModule;
    private PresenterModule presenterModule;


    public GoogleApiClient getGoogleApiClient() {
        return googleClientModule.getGoogleApiClient();
    }

    private Injection() {
    }

    public Injection(InjectionBuilder builder) {
        this.repositoryModule = builder.repositoryModule;
        this.googleClientModule = builder.googleClientModule;
        this.providerModule = builder.providerModule;
        this.presenterModule = builder.presenterModule;
    }

    public IGeoCodingRepository getGeoCodingRepository() {
        return repositoryModule.getGeoCodingRepository();
    }


    public ISuggestionRepository getSuggestionsRepository() {
        return providerModule.getSuggestionsProvider();
    }


    public NiboPickerContracts.Presenter getNiboPickerPresenter() {
        return presenterModule.getNiboPickerPresenter();
    }

    public OriginDestinationContracts.Presenter getOriginDestinationPickerPresenter() {
        return presenterModule.getOriginDestinationPickerPresenter();
    }

    public LocationRequest getLocationRequest() {
        return googleClientModule.getLocationRequest();
    }


    public static class InjectionBuilder {
        public APIModule apiModule;
        public RepositoryModule repositoryModule;
        public GoogleClientModule googleClientModule;
        public RetrofitModule retrofitModule;
        public InteractorModule interactorModule;
        public ProviderModule providerModule;
        public PresenterModule presenterModule;

        private Context context;

        public InjectionBuilder setInteractorModule(InteractorModule interactorModule) {
            this.interactorModule = interactorModule;
            return this;
        }

        public InjectionBuilder setAPIModule(APIModule apiModule) {
            this.apiModule = apiModule;
            return this;
        }

        public InjectionBuilder setRepositoryModule(RepositoryModule repositoryModule) {
            this.repositoryModule = repositoryModule;
            return this;
        }

        public InjectionBuilder setPresenterModule(PresenterModule presenterModule) {
            this.presenterModule = presenterModule;
            return this;
        }

        public InjectionBuilder setContext(Context context) {
            this.context = context;
            return this;
        }

        public InjectionBuilder setGoogleClientModule(GoogleClientModule googleClientModule) {
            this.googleClientModule = googleClientModule;
            return this;
        }

        public InjectionBuilder setRetrofitModule(RetrofitModule retrofitModule) {
            this.retrofitModule = retrofitModule;
            return this;
        }

        public InjectionBuilder setProviderModule(ProviderModule providerModule) {
            this.providerModule = providerModule;
            return this;
        }

        public Injection build() {
            if (googleClientModule == null) {
                throw new IllegalStateException("Please set GoogleAPI module, it has external dependencies");
            }
            if (providerModule == null) {
                if (context == null) {
                    throw new IllegalStateException("Please set context, shit depends on it");
                } else {
                    providerModule = new ProviderModule(googleClientModule.getGoogleApiClient(), context);
                }
            }
            if (retrofitModule == null) {
                retrofitModule = RetrofitModule.getInstance();
            }
            if (apiModule == null) {
                apiModule = APIModule.getInstance(retrofitModule);
            }
            if (repositoryModule == null) {
                repositoryModule = RepositoryModule.getInstance(apiModule);
            }
            if (interactorModule == null) {
                interactorModule = new InteractorModule(repositoryModule, providerModule);
            }
            if (presenterModule == null) {
                presenterModule = new PresenterModule(interactorModule);
            }

            return new Injection(this);
        }
    }


}
