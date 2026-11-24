import { useState } from 'react';
import HotelList from './pages/HotelList.jsx';
import HotelDetails from './pages/HotelDetails.jsx';
import Confirmation from './pages/Confirmation.jsx';
import FindBooking from './pages/FindBooking.jsx';

export default function App() {
  // view: { name: 'list' | 'hotel' | 'confirmation' | 'find', ... }
  const [view, setView] = useState({ name: 'list' });
  // The chosen city and dates are shared by the list and the hotel page
  const [search, setSearch] = useState({ city: '', checkIn: '', checkOut: '' });

  const go = (next) => {
    setView(next);
    window.scrollTo(0, 0);
  };

  return (
    <>
      <header className="top">
        <button className="brand" onClick={() => go({ name: 'list' })}>Stayfinder</button>
        <nav aria-label="Main">
          <button className={view.name === 'find' ? '' : 'on'} onClick={() => go({ name: 'list' })}>Hotels</button>
          <button className={view.name === 'find' ? 'on' : ''} onClick={() => go({ name: 'find' })}>Find my booking</button>
        </nav>
      </header>

      <main>
        {view.name === 'list' && (
          <HotelList search={search} setSearch={setSearch} onOpen={(id) => go({ name: 'hotel', id })} />
        )}
        {view.name === 'hotel' && (
          <HotelDetails
            hotelId={view.id}
            search={search}
            setSearch={setSearch}
            onBack={() => go({ name: 'list' })}
            onBooked={(booking) => go({ name: 'confirmation', booking })}
          />
        )}
        {view.name === 'confirmation' && (
          <Confirmation booking={view.booking} onDone={() => go({ name: 'list' })} onFind={() => go({ name: 'find' })} />
        )}
        {view.name === 'find' && <FindBooking />}
      </main>

      <footer className="foot">Stayfinder is a demo project. No real payments or bookings are made.</footer>
    </>
  );
}
