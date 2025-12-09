module.exports = {
  content: ["./index.html", "./src/**/*.{js,ts,jsx,tsx}"],
  theme: {
    extend: {
      fontFamily: {
        display: ['"Poppins"', "system-ui", "sans-serif"],
        sans: ['"Inter"', "system-ui", "sans-serif"],
      },
      screens: {
        'xs': '475px',
      },
      spacing: {
        '18': '4.5rem',
        '88': '22rem',
      },
    },
  },
  plugins: [require("daisyui")],
  daisyui: {
    themes: [
      {
        light: {
          primary: "#F3473F",
          "primary-content": "#FFFFFF",
          secondary: "#F96C00",
          "secondary-content": "#FFFFFF",
          accent: "#A9CCDB",
          neutral: "#261C1A",
          "neutral-content": "#FDFDFE",
          "base-100": "#FDFDFE",
          "base-200": "#E4E8EA",
          "base-300": "#C6CDD1",
          info: "#3ABFF8",
          success: "#22C55E",
          warning: "#FACC15",
          error: "#EF4444",
        },
        dark: {
          primary: "#F3473F",
          "primary-content": "#FFFFFF",
          secondary: "#F96C00",
          "secondary-content": "#FFFFFF",
          accent: "#A9CCDB",
          neutral: "#261C1A",
          "neutral-content": "#FDFDFE",
          "base-100": "#FDFDFE",
          "base-200": "#E4E8EA",
          "base-300": "#C6CDD1",
          info: "#3ABFF8",
          success: "#22C55E",
          warning: "#FACC15",
          error: "#EF4444",
        },
      },
    ],
    base: false,
    styled: true,
    utils: true,
  },
};
