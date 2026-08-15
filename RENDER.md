# Deploying IntervU

## 1. Backend on Render

1. In Render ([render.com](https://render.com)), choose **New → Blueprint** and connect your GitHub repository (`siddharthdey27/IntervU`).
2. Select the repository's `main` branch. Render will read `render.yaml` and create the `interv-u-api` service.
3. Enter the `sync: false` values when prompted (Supabase DB credentials, API keys).
4. Note your API URL (e.g., `https://interv-u-api.onrender.com`).

## 2. Frontend on Vercel

1. In Vercel ([vercel.com](https://vercel.com)), click **Add New → Project** and import the `siddharthdey27/IntervU` repository.
2. Configure project settings:
   - **Root Directory**: `frontend`
   - **Framework Preset**: Vite
   - **Build Command**: `npm run build`
   - **Output Directory**: `dist`
3. Add Environment Variable:
   - `VITE_API_BASE_URL` = `https://<your-render-api-url>/api` (e.g. `https://interv-u-api.onrender.com/api`)
4. Click **Deploy**.

## 3. Link CORS on Render

Once your Vercel frontend URL is live (e.g., `https://interv-u.vercel.app`):
1. Go to your `interv-u-api` service on Render dashboard.
2. In **Environment**, set `CORS_ALLOWED_ORIGINS` to your Vercel URL (e.g., `https://interv-u.vercel.app`).
3. Trigger a manual redeploy on Render.
