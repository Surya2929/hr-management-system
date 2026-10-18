/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      // Urbanist: free Google Font that looks close to the design's Lufga
      fontFamily: {
        sans: ['Urbanist', 'ui-sans-serif', 'system-ui', 'sans-serif'],
      },
      // Brand orange from the design. 500 = fills, 600+ = text on light backgrounds (readable contrast)
      colors: {
        brand: {
          50:  '#FFF5EE',
          100: '#FFE8D9',
          200: '#FFCFB0',
          300: '#FFAD7D',
          400: '#FF8645',
          500: '#F26419',
          600: '#C8470A',
          700: '#A63A0B',
          800: '#84300F',
          900: '#6B2A10',
          950: '#3A1205',
        },
      },
      borderRadius: {
        '4xl': '2rem',
      },
    },
  },
  plugins: [],
}
