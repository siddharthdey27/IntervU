# Deploying IntervU on Render

1. In Render, choose **New → Blueprint** and connect the GitHub repository.
2. Select the repository's `main` branch. Render will read `render.yaml` and create the API and frontend services.
3. Enter the `sync: false` values when prompted. Keep secrets in Render, never in Git.
4. After the frontend service URL is known, set the API service's `CORS_ALLOWED_ORIGINS` to that exact `https://...onrender.com` URL, then redeploy the API.
5. Set the frontend service's `VITE_API_BASE_URL` to the API URL plus `/api`, for example `https://inter-u-api.onrender.com/api`, then redeploy the frontend.
6. Run `backend/src/main/resources/db/migration/V1__init.sql` and subsequent migrations against the production Postgres/Supabase database before first use. Promote the first admin with `backend/src/main/resources/db/bootstrap_admin.sql`.

The API health check is `GET /health`. Render supplies `PORT`; the backend reads it automatically.
