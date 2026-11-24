import { useEffect, useMemo, useState } from 'react';
import { searchHotels } from '../api.js';
import { today, money, stars, amenityIcon, coverStyle, formatDate } from '../utils.js';

const CITIES = ['Goa', 'Mumbai', 'Jaipur', 'Delhi', 'Kochi'];

export default function HotelList({ search, setSearch, onOpen }) {
  const [draft, setDraft] = useState(search); // what is typed in the form
  const [formError, setFormError] = useState('');
  const [hotels, setHotels] = useState(null);
  const [error, setError] = useState('');
  const [sort, setSort] = useState('name');
  const [minStars, setMinStars] = useState(0);

  // Load whenever the applied search changes
  useEffect(() => {
    let ignore = false;
    setHotels(null);
    setError('');
    searchHotels(search)
      .then((data) => !ignore && setHotels(data))
      .catch((e) => !ignore && setError(e.message));
    return () => { ignore = true; };
  }, [search]);

  const apply = (next) => {
    setFormError('');
    if ((next.checkIn && !next.checkOut) || (!next.checkIn && next.checkOut)) {
      return setFormError('Select both check-in and check-out dates, or leave both empty.');
    }
    if (next.checkIn && next.checkOut <= next.checkIn) {
      return setFormError('Check-out must be after check-in.');
    }
    setSearch({ ...next, city: next.city.trim() });
  };

  const pickCity = (city) => {
    const next = { ...draft, city };
    setDraft(next);
    apply(next);
  };

  const shown = useMemo(() => {
    if (!hotels) return null;
    const list = hotels.filter((h) => h.stars >= minStars);
    const by = {
      name: (a, b) => a.name.localeCompare(b.name),
      priceLow: (a, b) => a.pricePerNight - b.pricePerNight,
      priceHigh: (a, b) => b.pricePerNight - a.pricePerNight,
      stars: (a, b) => b.stars - a.stars,
    };
    return [...list].sort(by[sort]);
  }, [hotels, sort, minStars]);

  const hasDates = search.checkIn && search.checkOut;

  return (
    <>
      <section className="hero">
        <h1>Find a room in India's favourite cities</h1>
        <p className="hero-sub">Search by city and dates to see the total price and rooms left.</p>

        <form className="searchbar" onSubmit={(e) => { e.preventDefault(); apply(draft); }}>
          <label>City
            <input value={draft.city} onChange={(e) => setDraft({ ...draft, city: e.target.value })}
                   placeholder="Goa, Mumbai, Jaipur…" />
          </label>
          <label>Check-in
            <input type="date" min={today()} value={draft.checkIn}
                   onChange={(e) => setDraft({ ...draft, checkIn: e.target.value })} />
          </label>
          <label>Check-out
            <input type="date" min={draft.checkIn || today()} value={draft.checkOut}
                   onChange={(e) => setDraft({ ...draft, checkOut: e.target.value })} />
          </label>
          <button className="btn primary">Search hotels</button>
        </form>
        {formError && <p className="msg error on-dark">{formError}</p>}

        <div className="chips" role="group" aria-label="Popular cities">
          <button className={'chip' + (!search.city ? ' active' : '')} onClick={() => pickCity('')}>All cities</button>
          {CITIES.map((c) => (
            <button key={c} className={'chip' + (search.city.toLowerCase() === c.toLowerCase() ? ' active' : '')}
                    onClick={() => pickCity(c)}>{c}</button>
          ))}
        </div>
      </section>

      <div className="toolbar">
        <p className="muted" role="status">
          {shown ? `${shown.length} hotel${shown.length === 1 ? '' : 's'}` : 'Searching…'}
          {hasDates && ` for ${formatDate(search.checkIn)} to ${formatDate(search.checkOut)}`}
        </p>
        <div className="toolbar-controls">
          <label>Sort by
            <select value={sort} onChange={(e) => setSort(e.target.value)}>
              <option value="name">Name</option>
              <option value="priceLow">Price: low to high</option>
              <option value="priceHigh">Price: high to low</option>
              <option value="stars">Star rating</option>
            </select>
          </label>
          <label>Rating
            <select value={minStars} onChange={(e) => setMinStars(Number(e.target.value))}>
              <option value={0}>Any</option>
              <option value={3}>3 stars and up</option>
              <option value={4}>4 stars and up</option>
              <option value={5}>5 stars</option>
            </select>
          </label>
        </div>
      </div>

      {error && <p className="msg error">{error}</p>}

      {!shown && !error && (
        <div className="list">{[1, 2, 3].map((i) => <div key={i} className="card skeleton" />)}</div>
      )}

      {shown?.length === 0 && (
        <div className="empty">
          <h2>No hotels found</h2>
          <p>
            {hotels?.length === 0 && !search.city
              ? 'The database has no hotels yet. Restart the backend so it can load the sample data.'
              : 'Try another city, or clear the rating filter.'}
          </p>
        </div>
      )}

      <div className="list">
        {shown?.map((h) => {
          const soldOut = h.stay && h.stay.availableRooms === 0;
          return (
            <article className="card" key={h.id}>
              <div className="cover" style={coverStyle(h.city)}>
                <span className="cover-city">{h.city}</span>
                <span className="cover-stars" aria-label={`${h.stars} stars`}>{stars(h.stars)}</span>
              </div>

              <div className="card-body">
                <h2>{h.name}</h2>
                <p className="where">{h.address}</p>
                <p>{h.description}</p>
                <ul className="amenities">
                  {h.amenities.slice(0, 5).map((a) => (
                    <li key={a}><span aria-hidden="true">{amenityIcon(a)}</span> {a}</li>
                  ))}
                </ul>
              </div>

              <div className="card-side">
                <div>
                  <strong className="price">{money(h.pricePerNight)}</strong>
                  <span className="muted"> / night</span>
                </div>
                {h.stay && !soldOut && (
                  <div className="quote">
                    <strong>{money(h.stay.totalAmount)}</strong>
                    <span className="muted"> for {h.stay.nights} night{h.stay.nights > 1 ? 's' : ''}</span>
                  </div>
                )}
                {h.stay && (
                  <span className={'tag' + (soldOut ? ' full' : h.stay.availableRooms <= 2 ? ' warn' : '')}>
                    {soldOut ? 'Sold out for these dates'
                      : h.stay.availableRooms <= 2 ? `Only ${h.stay.availableRooms} left` : `${h.stay.availableRooms} rooms left`}
                  </span>
                )}
                <button className="btn primary" onClick={() => onOpen(h.id)}>
                  {soldOut ? 'See other dates' : 'View and book'}
                </button>
              </div>
            </article>
          );
        })}
      </div>
    </>
  );
}
