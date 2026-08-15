# Deploying IntervU on Render

You can deploy both the **Spring Boot Backend** and **React Vite Frontend** on Render using the included [render.yaml](file:///e:/IntervU/render.yaml) Blueprint specification.

---

## Step 1: Deploy with Render Blueprint

1. Push your latest changes to GitHub (`siddharthdey27/IntervU`).
2. Go to your [Render Dashboard](https://dashboard.render.com/).
3. Click **New +** → **Blueprint**.
4. Connect and select your **`IntervU`** repository.
5. Render will automatically detect `render.yaml` and create two services:
   - **`interv-u-api`** (Docker Web Service for Spring Boot)
   - **`interv-u-web`** (Static Site for React frontend)

---

## Step 2: Configure Environment Variables

When prompted during blueprint setup (or in each service's **Environment** tab later):

### Backend (`interv-u-api`)
| Variable | Value / Description |
|---|---|
| `SUPABASE_DB_URL` | JDBC URL (e.g. `jdbc:postgresql://<host>:5432/<database>`) |
| `SUPABASE_DB_USER` | Supabase Postgres username (e.g. `postgres`) |
| `SUPABASE_DB_PASSWORD` | Supabase Postgres password |
| `GEMINI_API_KEY` | Your Google Gemini API Key |
| `OPENAI_API_KEY` | *(Optional if AI_PROVIDER=gemini)* |
| `JUDGE0_BASE_URL` | RapidAPI Judge0 URL (or custom Judge0 instance) |
| `CORS_ALLOWED_ORIGINS` | Your frontend Render URL (e.g., `https://interv-u-web.onrender.com`) |

### Frontend (`interv-u-web`)
| Variable | Value / Description |
|---|---|
| `VITE_API_BASE_URL` | `https://interv-u-api.onrender.com/api` (replace with your backend URL) |

---

## Step 3: Link CORS & Verify

1. Once deployed, copy your frontend domain: `https://interv-u-web.onrender.com`.
2. Go to **`interv-u-api`** → **Environment** → set `CORS_ALLOWED_ORIGINS` = `https://interv-u-web.onrender.com`.
3. If `VITE_API_BASE_URL` on the frontend needs updating, set it to `https://interv-u-api.onrender.com/api` and click **Manual Deploy** → **Clear build cache & deploy**.
4. Test the health endpoint: `https://interv-u-api.onrender.com/health` (should return UP status).

