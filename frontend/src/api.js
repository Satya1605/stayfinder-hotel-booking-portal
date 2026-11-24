// All calls to the Spring Boot backend live here.
async function call(url, options) {
  let res;
  try {
    res = await fetch(url, options);
  } catch {
    throw new Error('Cannot reach the server. Check that the backend is running on port 8080.');
  }
  const data = await res.json().catch(() => ({}));
  if (!res.ok) throw new Error(data.message || 'Something went wrong. Please try again.');
  return data;
}

// Builds ?a=1&b=2 and leaves out empty values
const qs = (params) => {
  const clean = Object.entries(params).filter(([, v]) => v);
  return clean.length ? '?' + new URLSearchParams(clean).toString() : '';
};

export const searchHotels = ({ city, checkIn, checkOut }) =>
  call('/api/hotels' + qs({ city, checkIn, checkOut }));

export const getHotel = (id, { checkIn, checkOut } = {}) =>
  call(`/api/hotels/${id}` + qs({ checkIn, checkOut }));

export const getAlternatives = (id, { checkIn, checkOut }) =>
  call(`/api/hotels/${id}/alternatives` + qs({ checkIn, checkOut }));

export const createBooking = (body) =>
  call('/api/bookings', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  });

export const findBooking = (reference, email) =>
  call(`/api/bookings/${encodeURIComponent(reference.trim())}` + qs({ email: email.trim() }));

export const cancelBooking = (reference, email) =>
  call(`/api/bookings/${encodeURIComponent(reference.trim())}/cancel` + qs({ email: email.trim() }), {
    method: 'POST',
  });
