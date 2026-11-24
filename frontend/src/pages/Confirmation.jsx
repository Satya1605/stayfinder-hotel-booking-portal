import { useState } from 'react';
import { money, formatDateLong } from '../utils.js';

export default function Confirmation({ booking: b, onDone, onFind }) {
  const [copied, setCopied] = useState(false);

  const copy = async () => {
    try {
      await navigator.clipboard.writeText(b.reference);
      setCopied(true);
    } catch { /* clipboard not available: the reference is visible anyway */ }
  };

  return (
    <section className="panel confirm">
      <p className="tag">Booking confirmed</p>
      <h1>You're booked at {b.hotelName}</h1>
      <p>Keep this reference. You need it together with your email to view or cancel the booking.</p>

      <div className="ref-box">
        <span className="ref" aria-label="Booking reference">{b.reference}</span>
        <button className="btn" onClick={copy}>{copied ? 'Copied' : 'Copy'}</button>
      </div>

      <dl className="details">
        <dt>Hotel</dt><dd>{b.hotelName}, {b.city}</dd>
        <dt>Guest</dt><dd>{b.guestName} ({b.guestEmail})</dd>
        <dt>Check-in</dt><dd>{formatDateLong(b.checkIn)}</dd>
        <dt>Check-out</dt><dd>{formatDateLong(b.checkOut)}</dd>
        <dt>Stay</dt><dd>{b.nights} night{b.nights > 1 ? 's' : ''} × {money(b.pricePerNight)}</dd>
        <dt>Total</dt><dd><strong>{money(b.totalAmount)}</strong></dd>
      </dl>

      <div className="actions">
        <button className="btn" onClick={() => window.print()}>Print or save as PDF</button>
        <button className="btn" onClick={onFind}>View or cancel this booking</button>
        <button className="btn primary" onClick={onDone}>Back to hotels</button>
      </div>
    </section>
  );
}
