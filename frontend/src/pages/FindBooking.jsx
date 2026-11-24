import { useState } from 'react';
import { findBooking, cancelBooking } from '../api.js';
import { money, formatDateLong } from '../utils.js';

function lastBooking() {
  try { return JSON.parse(localStorage.getItem('lastBooking')) || {}; } catch { return {}; }
}

export default function FindBooking() {
  const saved = lastBooking();
  const [reference, setReference] = useState(saved.reference || '');
  const [email, setEmail] = useState(saved.email || '');
  const [booking, setBooking] = useState(null);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');
  const [confirming, setConfirming] = useState(false);
  const [busy, setBusy] = useState(false);

  const find = async (e) => {
    e.preventDefault();
    setError(''); setNotice(''); setConfirming(false); setBusy(true);
    try {
      setBooking(await findBooking(reference, email));
    } catch (err) {
      setBooking(null);
      setError(err.message);
    } finally {
      setBusy(false);
    }
  };

  const cancel = async () => {
    setError(''); setBusy(true);
    try {
      setBooking(await cancelBooking(booking.reference, booking.guestEmail));
      setNotice('Your booking is cancelled and the room has been released.');
    } catch (err) {
      setError(err.message);
    } finally {
      setConfirming(false);
      setBusy(false);
    }
  };

  return (
    <section className="panel narrow">
      <h1>Find my booking</h1>
      <p className="muted">Enter the reference from your confirmation and the email you booked with.</p>

      <form onSubmit={find} className="form">
        <label>Booking reference
          <input required value={reference} onChange={(e) => setReference(e.target.value)} placeholder="HB-XXXXXXXX" />
        </label>
        <label>Email
          <input type="email" required value={email} onChange={(e) => setEmail(e.target.value)} />
        </label>
        <button className="btn primary" disabled={busy}>{busy && !booking ? 'Searching…' : 'Find booking'}</button>
      </form>

      {error && <p className="msg error">{error}</p>}
      {notice && <p className="msg ok">{notice}</p>}

      {booking && (
        <>
          <div className="booking-head">
            <span className="ref">{booking.reference}</span>
            <span className={'tag' + (booking.status === 'CANCELLED' ? ' full' : '')}>
              {booking.status === 'CANCELLED' ? 'Cancelled' : 'Confirmed'}
            </span>
          </div>
          <dl className="details">
            <dt>Hotel</dt><dd>{booking.hotelName}, {booking.city}</dd>
            <dt>Guest</dt><dd>{booking.guestName}</dd>
            <dt>Check-in</dt><dd>{formatDateLong(booking.checkIn)}</dd>
            <dt>Check-out</dt><dd>{formatDateLong(booking.checkOut)}</dd>
            <dt>Stay</dt><dd>{booking.nights} night{booking.nights > 1 ? 's' : ''}</dd>
            <dt>Total</dt><dd><strong>{money(booking.totalAmount)}</strong></dd>
          </dl>

          {booking.cancellable && !confirming && (
            <button className="btn danger" onClick={() => setConfirming(true)}>Cancel this booking</button>
          )}
          {confirming && (
            <div className="confirm-box" role="alertdialog" aria-label="Confirm cancellation">
              <p><strong>Cancel this booking?</strong> The room will be released and this cannot be undone.</p>
              <button className="btn danger" onClick={cancel} disabled={busy}>{busy ? 'Cancelling…' : 'Yes, cancel it'}</button>
              <button className="btn" onClick={() => setConfirming(false)} disabled={busy}>Keep booking</button>
            </div>
          )}
          {!booking.cancellable && booking.status === 'CONFIRMED' && (
            <p className="muted">This stay has already started, so it can no longer be cancelled.</p>
          )}
        </>
      )}
    </section>
  );
}
