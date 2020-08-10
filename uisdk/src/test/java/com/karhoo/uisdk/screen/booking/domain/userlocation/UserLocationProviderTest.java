    @Mock
    UserLocation.OnPositionUpdated locationUpdated;
    @Mock
    AddressService addressService;
    @Mock
    LocationDetails locationDetails;
    @Mock
    Call<LocationDetails> call;
    @Mock
    LocationService locationService;
    @Mock
    Call<LocationDetails> locationDetailsCall;
    @Mock
    LiveBookingStatus bookingStatus;
    @Mock
    UserLocation.LocationSettingsCallback settingsCallback;

    @Captor
    ArgumentCaptor<UserLocation.LocationSettingsCallback> settingsCaptor;

    @Mock
    Popups snackbar;

    /**
     * When    Initialized
     * Then    The location provider should be correctly configured
     */
    @Test
    public void locationProviderShouldBeConfiguredCorrectlyAtStart() {
        locationProvider.checkLocationServices();
        verify(locationService).checkLocationSettings(locationProvider);
    }

    /**
     * Given    A user has denied location permissions
     * When    Getting the last know position
     * Then    Null should be the last known position of the location manager
     */
    @Test
    public void gettingPositionManuallyReturnsTheLastKnownPosition() {
        when(locationService.getLastKnownPosition()).thenReturn(locationWrapper);
        locationProvider = new UserLocationProvider(locationService, addressService, analytics);
        Assert.assertNotNull(locationProvider.getLastKnownPosition());
    }

    /**
     * Given    A user has allowed location permissions
     * When    Getting the last know position
     * Then    An analytical event for reverse geo should be sent
     */
    @Test
    public void gettingReverseGeoShouldFireRequestOnSuccess() {
        when(locationService.getLastKnownPosition()).thenReturn(locationWrapper);
        when(addressService.reverseGeolocate((LatLng) any())).thenReturn(call);
        doAnswer(new LocationUpdateListenerAnswer() {
            @Override
            void answer(KarhooCallback<LocationDetails> listener) {
                listener.onServiceResponse(locationDetails);
            }
        }).when(call).execute((KarhooCallback<LocationDetails>) any());
        ObservableVariable<LiveBookingStatus> observableVariable = new ObservableVariable<>();
        observableVariable.setValue(bookingStatus);
        locationProvider.setCurrentBookingStatus(observableVariable);
        locationProvider.updateLastKnownPosition();
        verify(analytics).reverseGeoResponse(locationDetails);
    }

    /**
     * Given   A a user has allowed there location has
     * When    A new location is received
     * Then    The location should be passed to the callback
     */
    @Test
    public void onLocationUpdateShouldReturnNewLocationThroughCallback() {
        locationProvider = new UserLocationProvider(locationService, addressService, analytics);

        doAnswer(new LocationSettingsListenerAnswer() {
            @Override
            void answer(UserLocation.LocationSettingsCallback listener) {
                listener.locationUpdatesAllowed();
            }
        }).when(locationService).checkLocationSettings(locationProvider);

        when(locationService.getLastKnownPosition()).thenReturn(locationWrapper);

        locationProvider.checkLocationServices();
        locationProvider.setUserSettingsCallback(settingsCallback);
        Assert.assertNotNull(locationProvider.getLastKnownPosition());
    }

    /**
     * Given   A callback has been set
     * When    An empty location is received
     * Then    The location should not be passed to the callback
     */
    @Test
    public void dontUpdateLocationIfLocationIsNull() {
        locationProvider.setLocationUpdatesCallback(locationUpdated);
        verify(locationUpdated, never()).locationUpdated((LocationWrapper) any());
    }

    /**
     * Given   A callback has not been set
     * When    A new location is received
     * Then    Nothing should happen
     */
    @Test
    public void whenNoCallbackIsSetNoUpdateShouldHappen() {
        locationProvider.setLocationUpdatesCallback(null);
        verify(locationUpdated, never()).locationUpdated((LocationWrapper) any());
    }

    /**
     * Given:   Snackbar is set
     * When:    getting updated position, the sdk returns onServiceError
     * Then:    showTemporaryError from snackbar
     */
    @Test
    public void showTemporaryErrorWhenOnServiceError() {
        when(locationService.getLastKnownPosition()).thenReturn(locationWrapper);

        locationProvider.setErrorView(snackbar);
        when(addressService.reverseGeolocate(any(LatLng.class)))
                .thenReturn(locationDetailsCall);
        doAnswer(new LocationDetailsListenerAnswer() {
            @Override
            void answer(Callback<LocationDetails> callback) {
                callback.onServiceError(new KarhooError(ErrorKind.UNKNOWN, 1, "error message", "error"));
            }
        }).when(locationDetailsCall).execute(any(KarhooCallback.class));

        ObservableVariable<LiveBookingStatus> observableVariable = new ObservableVariable<>();
        observableVariable.setValue(bookingStatus);
        locationProvider.setCurrentBookingStatus(observableVariable);
        locationProvider.updateLastKnownPosition();

        verify(snackbar).showTemporaryError(anyInt());
    }

    /**
     * Given:   The location provider is asked to get last location
     * When:    retrieving the location from the device
     * Then:    a non null locationwrapper should be returned
     */
    @Test
    public void gettingLastKnownLocationReturnsValidLocation() throws Exception {
        when(locationService.getLastKnownPosition()).thenReturn(locationWrapper);
        LocationWrapper wrapper = locationProvider.getLastKnownPosition();
        Assert.assertNotNull(wrapper);
    }

    /**
     * Given:   The location updates are set
     * When:    The location details are allowed
     * Then:    The callback should alert location is allowed
     */
    @Test
    public void settingTheLocationSettingsCallbackCallsBackAllowedIfAllowed() throws Exception {
        doAnswer(new LocationSettingsListenerAnswer() {
            @Override
            void answer(UserLocation.LocationSettingsCallback callback) {
                callback.locationUpdatesAllowed();
            }
        }).when(locationService).checkLocationSettings((UserLocation.LocationSettingsCallback) any());
        locationProvider.setLocationSettingsCallback(settingsCallback);
        locationProvider.checkLocationServices();

        verify(settingsCallback).locationUpdatesAllowed();
    }

    /**
     * When:   A request is made to stop getting updates
     * Then:   the request should be forwarded to the location service
     */
    @Test
    public void cancellingLocationUpdatesForwardsTheCall() throws Exception {
        locationProvider.stopLocationUpdates();
        verify(locationService).stopGettingLocationUpdates();
    }

    /**
     * When:   A request is made to start getting updates
     * Then:   the request should be forwarded to the location service
     */
    @Test
    public void startingLocationUpdatesForwardsTheCall() throws Exception {
        locationProvider.startLocationUpdates();
        verify(locationService).startGettingLocationUpdates();
    }

    /**
     * Given:   A settings callback has been set
     * When:    A request is made to monitor the settings
     * Then:    The call should be made on the set callback
     */
    @Test
    public void settingsAllowedCallsBackOnTheCorrectCallback() throws Exception {
        locationProvider.setLocationSettingsCallback(settingsCallback);
        locationProvider.locationUpdatesAllowed();
        verify(settingsCallback).locationUpdatesAllowed();
    }

    /**
     * Given:   A settings callback has been set
     * When:    A request is made to monitor the settings
     * And:     The settings are denied
     * Then:    The call should be made on the set callback
     */
    @Test
    public void settingsDeniedCallsBackOnTheCorrectCallback() throws Exception {
        locationProvider.setLocationSettingsCallback(settingsCallback);
        locationProvider.locationUpdatesDenied();
        verify(settingsCallback).locationUpdatesDenied();
    }

    /**
     * Given:   A location update callback has been set
     * When:    A request is made to monitor the users location
     * Then:    The call should be made on the set callback
     */
    @Test
    public void locationUpdatedCallsBackOnTheCorrectCallback() throws Exception {
        locationProvider.setLocationUpdatesCallback(locationUpdated);
        locationProvider.locationUpdated(locationWrapper);
        verify(locationUpdated).locationUpdated(locationWrapper);
    }

    private static abstract class LocationUpdateListenerAnswer implements Answer {

        @Override
        public Object answer(InvocationOnMock invocation) throws Throwable {
            KarhooCallback<LocationDetails> karhooCallback = (KarhooCallback<LocationDetails>) invocation.getArguments()[0];
            answer(karhooCallback);
            return null;
        }

        abstract void answer(KarhooCallback<LocationDetails> listener);
    }

    private static abstract class LocationDetailsListenerAnswer implements Answer {

        @Override
        public Object answer(InvocationOnMock invocation) throws Throwable {
            Callback<LocationDetails> callback = (Callback<LocationDetails>) invocation.getArguments()[0];
            answer(callback);
            return null;
        }

        abstract void answer(Callback<LocationDetails> callback);
    }


    private static abstract class LocationSettingsListenerAnswer implements Answer {

        @Override
        public Object answer(InvocationOnMock invocation) throws Throwable {
            UserLocation.LocationSettingsCallback callback = (UserLocation.LocationSettingsCallback) invocation.getArguments()[0];
            answer(callback);
            return null;
        }

        abstract void answer(UserLocation.LocationSettingsCallback callback);
    }

}

