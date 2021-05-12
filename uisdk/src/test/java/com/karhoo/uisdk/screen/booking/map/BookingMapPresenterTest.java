package com.karhoo.uisdk.screen.booking.map;

// TODO: Create kotlin file for this.

//@RunWith(MockitoJUnitRunner.class)
//public class BookingMapPresenterTest {

//    @Mock
//    BookingMapMVP.View view;
//    @Mock
//    BookingMapStategy.Presenter pickupPresenter;
//    @Mock
//    BookingMapStategy.Presenter pickupDropOffPresenter;
//    @Mock
//    LiveBookingStatus bookingStatus;
//    @Mock
//    com.karhoo.sdk.api.model.LatLng kLatLong;
//    @Mock
//    LocationDetails locationDetails;
//    @Mock
//    CompositeDisposableContainer compositeDisposable;
//    @Mock
//    Analytics analytics;
//
//    @Captor
//    ArgumentCaptor<BookingMapStategy.Owner> captor;
//
//    BookingMapPresenter presenter;
//    LatLng latLng;
//
//    @Before
//    public void setUp() throws Exception {
//        presenter = new BookingMapPresenter(view, pickupPresenter,
//                pickupDropOffPresenter, compositeDisposable, analytics);
//    }
//
//    /**
//     * Given:   The booking status is pickup only
//     * When:    Looking at the map
//     * Then:    The active presenter should be the pickupOnly presenter
//     */
//    @Test
//    public void presenterPickupShouldBeSetForTheCurrentBookingStatus() throws Exception {
//        latLng = new LatLng(0, 0);
//        when(locationDetails.getLatLng()).thenReturn(kLatLong);
//        when(kLatLong.getLongitude()).thenReturn(0D);
//        when(kLatLong.getLatitude()).thenReturn(0D);
//        LiveBookingStatus bookingStatus = new LiveBookingStatus();
//        bookingStatus.setPickup(locationDetails);
//        bookingStatus.setDestination(null);
//        ObservableVariable<LiveBookingStatus> observable = new ObservableVariable<>();
//        presenter.observeBookingStatus(observable);
//        observable.setValue(bookingStatus);
//        presenter.mapMoved(latLng);
//
//        verify(pickupPresenter, atLeastOnce()).mapMoved(latLng);
//        verify(pickupDropOffPresenter, never()).mapMoved(latLng);
//    }
//
//    /**
//     * Given:   The booking status is pickup only
//     * When:    Looking at the map
//     * Then:    The active presenter should be the pickupOnly presenter
//     */
//    @Test
//    public void presenterPickupDropoffShouldBeSetForTheCurrentBookingStatus() throws Exception {
//        latLng = new LatLng(0, 0);
//        LiveBookingStatus bookingStatus = new LiveBookingStatus();
//        bookingStatus.setPickup(locationDetails);
//        bookingStatus.setDestination(locationDetails);
//        ObservableVariable<LiveBookingStatus> observable = new ObservableVariable<>();
//        presenter.observeBookingStatus(observable);
//        observable.setValue(bookingStatus);
//        presenter.mapMoved(latLng);
//
//        verify(pickupDropOffPresenter, atLeastOnce()).mapMoved(latLng);
//        verify(pickupPresenter, never()).mapMoved(latLng);
//    }
//
//    /**
//     * Given:   The user clears the destination
//     * When:    The map reverts back to the pickup location
//     * Then:    The map should be asked to clear all the markers
//     */
//    @Test
//    public void markersGetClearedWhenClearingDestination() throws Exception {
//        when(locationDetails.getLatLng()).thenReturn(kLatLong);
//        when(kLatLong.getLongitude()).thenReturn(0D);
//        when(kLatLong.getLatitude()).thenReturn(0D);
//        LiveBookingStatus bookingStatus = new LiveBookingStatus();
//        bookingStatus.setPickup(locationDetails);
//        bookingStatus.setDestination(locationDetails);
//        ObservableVariable<LiveBookingStatus> observable = new ObservableVariable<>();
//        presenter.observeBookingStatus(observable);
//        observable.setValue(bookingStatus);
//        bookingStatus.setDestination(null);
//        observable.setValue(bookingStatus);
//
//        verify(view, atLeastOnce()).clearMarkers();
//
//    }
//
//    /**
//     * Given:   A user has pressed locate meh
//     * When:    That requested is passed on to the main presenter
//     * Then:    The presenter should forward that to the correct place
//     */
//    @Test
//    public void locateMehGoesToTheCorrectPresenter() throws Exception {
//        when(locationDetails.getLatLng()).thenReturn(kLatLong);
//        when(kLatLong.getLongitude()).thenReturn(0D);
//        when(kLatLong.getLatitude()).thenReturn(0D);
//        LiveBookingStatus bookingStatus = new LiveBookingStatus();
//        bookingStatus.setPickup(locationDetails);
//        bookingStatus.setDestination(null);
//        ObservableVariable<LiveBookingStatus> observable = new ObservableVariable<>();
//        presenter.observeBookingStatus(observable);
//        observable.setValue(bookingStatus);
//        presenter.locateUserPressed(new LatLng(0D, 0D));
//
//        verify(pickupPresenter, atLeastOnce()).locateUserPressed((LatLng) any());
//    }
//
//    /**
//     * Given:   A pickup and drop off is set
//     * When:    The map comes into view
//     * Then:    The map should be asked to zoom to the markers
//     */
//    @Test
//    public void mapZoomiesToMarkersWhenPickupAndDropoffAreSet() throws Exception {
//        when(locationDetails.getLatLng()).thenReturn(kLatLong);
//        when(kLatLong.getLongitude()).thenReturn(0D);
//        when(kLatLong.getLatitude()).thenReturn(0D);
//        doAnswer(new Answer() {
//            @Override
//            public Object answer(InvocationOnMock invocation) throws Throwable {
//                captor.getValue().zoomMapToMarkers();
//                return null;
//            }
//        }).when(pickupDropOffPresenter).locateUserPressed((LatLng) any());
//        LiveBookingStatus bookingStatus = new LiveBookingStatus();
//        bookingStatus.setPickup(locationDetails);
//        bookingStatus.setDestination(locationDetails);
//        ObservableVariable<LiveBookingStatus> observable = new ObservableVariable<>();
//        verify(pickupDropOffPresenter, atLeastOnce()).setOwner(captor.capture());
//        presenter.observeBookingStatus(observable);
//        observable.setValue(bookingStatus);
//        presenter.locateUserPressed(latLng);
//
//        verify(view, atLeastOnce()).addMarkers((com.karhoo.sdk.api.model.LatLng) any(), (com.karhoo.sdk.api.model.LatLng) any());
//        verify(pickupDropOffPresenter, atLeastOnce()).locateUserPressed((LatLng) any());
//    }
//
//    /**
//     * Given:   When a user drags the map
//     * When:    there is no destination set
//     * Then:    The appropriate presenter should be called
//     */
//    @Test
//    public void userDraggingMapGetsPassedOntoPresenter() throws Exception {
//        when(locationDetails.getLatLng()).thenReturn(kLatLong);
//        when(kLatLong.getLongitude()).thenReturn(0D);
//        when(kLatLong.getLatitude()).thenReturn(0D);
//        LiveBookingStatus bookingStatus = new LiveBookingStatus();
//        bookingStatus.setPickup(locationDetails);
//        bookingStatus.setDestination(null);
//        ObservableVariable<LiveBookingStatus> observable = new ObservableVariable<>();
//        presenter.observeBookingStatus(observable);
//        observable.setValue(bookingStatus);
//        presenter.zoom((LatLng) any());
//
//        verify(view, atLeastOnce()).zoom((LatLng) any());
//    }
//
//    /**
//     * Given:   A user moves the map
//     * When:    The event is captured
//     * Then:    Its passed to the correct presenter
//     */
//    @Test
//    public void whenMapIsMovedThePresenterIsAlerted() throws Exception {
//        when(locationDetails.getLatLng()).thenReturn(kLatLong);
//        when(kLatLong.getLongitude()).thenReturn(0D);
//        when(kLatLong.getLatitude()).thenReturn(0D);
//        LiveBookingStatus bookingStatus = new LiveBookingStatus();
//        bookingStatus.setPickup(locationDetails);
//        bookingStatus.setDestination(null);
//        ObservableVariable<LiveBookingStatus> observable = new ObservableVariable<>();
//        presenter.observeBookingStatus(observable);
//        observable.setValue(bookingStatus);
//        presenter.mapDragged();
//
//        verify(pickupPresenter, atLeastOnce()).mapDragged();
//    }
//
//    /**
//     * Given:   I only have a pickup set
//     * When:    I press locate me
//     * Then:    A call should be made to allow to be located
//     */
//    @Test
//    public void whenLocateAndUpdateCallToViewToLocateTheUser() throws Exception {
//        presenter.locateAndUpdate();
//        verify(view, atLeastOnce()).locationUpdatesAllowed();
//    }
//
//    /**
//     * Given:   There is an error
//     * When:    onError called
//     * Then:    error passed through to view
//     */
//    @Test
//    public void onErrorShowErrorInView() {
//        int errorId = R.string.kh_uisdk_booking_error;
//        presenter.onError(errorId);
//        verify(view).showError(errorId);
//    }
//
//    /**
//     * Given:   I only have a pickup set
//     * When:    I press locate me
//     * Then:    An analytic call will fire an event for reverse geo
//     */
//    @Test
//    public void whenLocateAndUpdateAnalyticsWillCallRevGeo() throws Exception {
//        when(locationDetails.getLatLng()).thenReturn(kLatLong);
//        when(kLatLong.getLongitude()).thenReturn(0D);
//        when(kLatLong.getLatitude()).thenReturn(0D);
//        LiveBookingStatus bookingStatus = new LiveBookingStatus();
//        bookingStatus.setPickup(locationDetails);
//        bookingStatus.setDestination(null);
//        ObservableVariable<LiveBookingStatus> observable = new ObservableVariable<>();
//        presenter.observeBookingStatus(observable);
//        observable.setValue(bookingStatus);
//        presenter.locateUserPressed(latLng);
//        verify(analytics).reverseGeo();
//    }
//
//
//    /**
//     * When:    onPause is called
//     * Then:    The CompositeDisposableContainer is disposed
//     */
//    @Test
//    public void compositeDisposableContainerIsDisposedOnStop() throws Exception {
//        presenter.onPause();
//        verify(compositeDisposable).dispose();
//    }

//}