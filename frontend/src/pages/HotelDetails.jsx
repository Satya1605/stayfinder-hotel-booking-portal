import { useEffect, useState } from 'react';
import { getHotel, getAlternatives, createBooking } from '../api.js';
import { today, money, stars, amenityIcon, coverStyle, formatDate } from '../utils.js';

export default function HotelDetails({ hotelId, search, setSearch, onBack, onBooked }) {
  const { checkIn, checkOut } = search;
  const [hotel, setHotel] = useState(null);
  const [loadError, setLoadError] = useState('');
  const [dateError, setDateError] = useState('');
  const [alternatives, setAlternatives] = useState(null); // null = not loaded
  const [guest, setGuest] = useState({ guestName: '', guestEmail: '' });
  const [submitError, setSubmitError] = useState('');
  const [busy, setBusy] = useState(false);
  const [refreshKey, setRefreshKey] = useState(0);

  // Reload the hotel (with price and availability) whenever the dates change
  useEffect(() => {
    let ignore = false;
    setDateError('');
    setAlternatives(null);

    const load = async () => {
      const datesChosen = checkIn && checkOut;
      if (datesChosen && checkOut <= checkIn) {
        setDateError('Check-out must be after check-in.');
        return setHotel(await getHotel(hotelId));
      }
      try {
        const h = await getHotel(hotelId, datesChosen ? { checkIn, checkOut } : {});
        if (ignore) return;
        setHotel(h);
        if (h.stay && h.stay.availableRooms === 0) {
          const alts = await getAlternatives(hotelId, { checkIn, checkOut });
          if (!ignore) setAlternatives(alts);
        }
      } catch (e) {
        if (ignore) return;
        if (datesChosen) { // most likely a date problem: still show the hotel, without a price
          setDateError(e.message);
          setHotel(await getHotel(hotelId));
        } else {
          setLoadError(e.message);
        }
      }
    };
    load().catch((e) => !ignore && setLoadError(e.message));
    return () => { ignore = true; };
  }, [hotelId, checkIn, checkOut, refreshKey]);

  const setDates = (changes) => setSearch({ ...search, ...changes });

  const onCheckIn = (value) => {
    // keep check-out valid: clear it if it is no longer after the new check-in
    setDates({ checkIn: value, checkOut: checkOut && checkOut > value ? checkOut : '' });
  };

  const submit = async (e) => {
    e.preventDefault();
    setSubmitError('');
    setBusy(true);
    try {
      const booking = await createBooking({
        hotelId,
        guestName: guest.guestName.trim(),
        guestEmail: guest.guestEmail.trim(),
        checkIn,
        checkOut,
      });
      try { localStorage.setItem('lastBooking', JSON.stringify({ reference: booking.reference, email: booking.guestEmail })); } catch { /* optional */ }
      onBooked(booking);
    } catch (err) {
      setSubmitError(err.message);
      setRefreshKey((k) => k + 1); // availability may have just changed, so look again
    } finally {
      setBusy(false);
    }
  };

  if (loadError) return <><button className="link" onClick={onBack}>Back to hotels</button><p className="msg error">{loadError}</p></>;
  if (!hotel) return <div className="card skeleton tall" />;

  const stay = hotel.stay;
  const soldOut = stay && stay.availableRooms === 0;

  return (
    <>
      <button className="link" onClick={onBack}>Back to hotels</button>

      <div className="details-grid">
        <section>
          <div className="cover wide" style={coverStyle(hotel.city)}>
            <span className="cover-city">{hotel.city}</span>
            <span className="cover-stars" aria-label={`${hotel.stars} stars`}>{stars(hotel.stars)}</span>
          </div>
          <h1>{hotel.name}</h1>
          <p className="where">{hotel.address}</p>
          <p className="lead">{hotel.description}</p>

          <h2>Amenities</h2>
          <ul className="amenities big">
            {hotel.amenities.map((a) => (
              <li key={a}><span aria-hidden="true">{amenityIcon(a)}</span> {a}</li>
            ))}
          </ul>

          <h2>Room</h2>
          <p>Standard room for 2 guests, {money(hotel.pricePerNight)} per night. This hotel has {hotel.totalRooms} rooms in total.</p>
        </section>

        <aside className="panel booking">
          <h2>Book your stay</h2>

          <div className="row">
            <label>Check-in
              <input type="date" min={today()} value={checkIn} onChange={(e) => onCheckIn(e.target.value)} />
            </label>
            <label>Check-out
              <input type="date" min={checkIn || today()} value={checkOut}
                     onChange={(e) => setDates({ checkOut: e.target.value })} />
            </label>
          </div>
          {dateError && <p className="msg error">{dateError}</p>}

          {!stay && !dateError && <p className="muted">Choose your dates to see the total price and availability.</p>}

          {stay && !soldOut && (
            <>
              <dl className="summary">
                <dt>{money(hotel.pricePerNight)} × {stay.nights} night{stay.nights > 1 ? 's' : ''}</dt>
                <dd>{money(stay.totalAmount)}</dd>
                <dt className="total">Total to pay at the hotel</dt>
                <dd className="total">{money(stay.totalAmount)}</dd>
              </dl>
              <p className={'msg ' + (stay.availableRooms <= 2 ? 'warn' : 'ok')}>
                {stay.availableRooms <= 2 ? `Only ${stay.availableRooms} room${stay.availableRooms > 1 ? 's' : ''} left for these dates.` : 'Rooms are available for these dates.'}
              </p>
            </>
          )}

          {soldOut && (
            <div className="suggest">
              <p className="msg error">No room is free from {formatDate(checkIn)} to {formatDate(checkOut)}.</p>
              {alternatives === null && <p className="muted">Looking for nearby dates…</p>}
              {alternatives && alternatives.length > 0 && (
                <>
                  <p><strong>These nearby dates are free for the same {stay.nights} night{stay.nights > 1 ? 's' : ''}:</strong></p>
                  <ul className="alts">
                    {alternatives.map((a) => (
                      <li key={a.checkIn}>
                        <button className="alt" onClick={() => setDates({ checkIn: a.checkIn, checkOut: a.checkOut })}>
                          <span>{formatDate(a.checkIn)} to {formatDate(a.checkOut)}</span>
                          <span className="muted">{Math.abs(a.daysFromRequested)} day{Math.abs(a.daysFromRequested) > 1 ? 's' : ''} {a.daysFromRequested < 0 ? 'earlier' : 'later'} · {money(a.totalAmount)}</span>
                        </button>
                      </li>
                    ))}
                  </ul>
                </>
              )}
              {alternatives && alternatives.length === 0 && (
                <p className="muted">Nothing is free within two weeks of these dates. Try another hotel from the list.</p>
              )}
            </div>
          )}

          <form onSubmit={submit} className="form">
            <label>Guest name
              <input required maxLength={100} value={guest.guestName}
                     onChange={(e) => setGuest({ ...guest, guestName: e.target.value })} autoComplete="name" />
            </label>
            <label>Email
              <input type="email" required maxLength={150} value={guest.guestEmail}
                     onChange={(e) => setGuest({ ...guest, guestEmail: e.target.value })} autoComplete="email" />
            </label>
            {submitError && <p className="msg error">{submitError}</p>}
            <button className="btn primary block" disabled={busy || !stay || soldOut}>
              {busy ? 'Confirming…' : stay && !soldOut ? `Confirm booking · ${money(stay.totalAmount)}` : 'Confirm booking'}
            </button>
          </form>
        </aside>
      </div>
    </>
  );
}
