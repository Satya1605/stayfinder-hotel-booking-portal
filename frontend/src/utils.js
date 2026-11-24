export const today = () => {
  const d = new Date();
  d.setMinutes(d.getMinutes() - d.getTimezoneOffset()); // local date, not UTC
  return d.toISOString().slice(0, 10);
};

export const money = (n) => '₹' + Number(n).toLocaleString('en-IN', { maximumFractionDigits: 0 });

export const formatDate = (iso) =>
  new Date(iso + 'T00:00:00').toLocaleDateString('en-IN', { day: 'numeric', month: 'short' });

export const formatDateLong = (iso) =>
  new Date(iso + 'T00:00:00').toLocaleDateString('en-IN', {
    weekday: 'short', day: 'numeric', month: 'short', year: 'numeric',
  });

export const stars = (n) => '★'.repeat(n) + '☆'.repeat(5 - n);

const AMENITY_ICONS = {
  'Pool': '🏊', 'Wi-Fi': '📶', 'Breakfast included': '🍳', 'Beach access': '🏖️',
  'Air conditioning': '❄️', 'Courtyard garden': '🌿', 'Sea view': '🌊', 'Spa': '💆',
  'Gym': '🏋️', 'Airport transfer': '🚐', 'Rooftop restaurant': '🍽️', 'Heritage tours': '🏛️',
  'Work lounge': '💼', '24-hour reception': '🛎️', 'Ayurvedic massage': '🌺',
};
export const amenityIcon = (name) => AMENITY_ICONS[name] || '✔';

// One colour pair per city for the hotel "cover"
const CITY_COLOURS = {
  Goa: ['#0e7c86', '#f0a202'],
  Mumbai: ['#1d3557', '#457b9d'],
  Jaipur: ['#9d2f4f', '#e07a5f'],
  Delhi: ['#3d405b', '#81b29a'],
  Kochi: ['#1b5e3b', '#7bb662'],
};
export const coverStyle = (city) => {
  const [a, b] = CITY_COLOURS[city] || ['#12303a', '#1f6f78'];
  return { background: `linear-gradient(135deg, ${a}, ${b})` };
};
