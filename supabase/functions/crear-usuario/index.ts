import { createClient } from 'npm:@supabase/supabase-js@2';

const corsHeaders = {
  'Access-Control-Allow-Origin': '*',
  'Access-Control-Allow-Headers': 'authorization, x-client-info, apikey, content-type',
};

Deno.serve(async (req: Request) => {
  if (req.method === 'OPTIONS') {
    return new Response('ok', { headers: corsHeaders });
  }

  try {
    const supabaseUrl = Deno.env.get('SUPABASE_URL');
    const serviceRoleKey = Deno.env.get('SUPABASE_SERVICE_ROLE_KEY');
    const authHeader = req.headers.get('Authorization');

    if (!supabaseUrl || !serviceRoleKey || !authHeader) {
      return json({ error: 'Configuración o autorización incompleta.' }, 401);
    }

    const admin = createClient(supabaseUrl, serviceRoleKey, {
      auth: { persistSession: false, autoRefreshToken: false }
    });

    const token = authHeader.replace('Bearer ', '');
    const { data: userData, error: userError } = await admin.auth.getUser(token);
    if (userError || !userData.user) {
      return json({ error: 'Sesión inválida.' }, 401);
    }

    const { data: caller, error: callerError } = await admin
      .from('usuarios')
      .select('activo,rol:roles(nombre)')
      .eq('id_usuario', userData.user.id)
      .single();

    const relation = Array.isArray(caller?.rol) ? caller?.rol[0] : caller?.rol;
    if (callerError || !caller?.activo || relation?.nombre !== 'ADMINISTRADOR') {
      return json({ error: 'Solo el administrador puede crear usuarios.' }, 403);
    }

    const body = await req.json();
    const nombres = String(body.nombres ?? '').trim();
    const apellidos = String(body.apellidos ?? '').trim();
    const email = String(body.email ?? '').trim().toLowerCase();
    const password = String(body.password ?? '');
    const telefono = String(body.telefono ?? '').trim();
    const rol = String(body.rol ?? 'VENDEDOR').toUpperCase();

    if (!nombres || !email || password.length < 8) {
      return json({ error: 'Nombres, correo y contraseña de al menos 8 caracteres son obligatorios.' }, 400);
    }
    if (!['ADMINISTRADOR', 'VENDEDOR'].includes(rol)) {
      return json({ error: 'Rol inválido.' }, 400);
    }

    const { data, error } = await admin.auth.admin.createUser({
      email,
      password,
      email_confirm: true,
      user_metadata: { nombres, apellidos, telefono, rol }
    });

    if (error) {
      return json({ error: error.message }, 400);
    }

    return json({ id: data.user.id, email: data.user.email }, 201);
  } catch (error) {
    return json({ error: error instanceof Error ? error.message : 'Error interno.' }, 500);
  }
});

function json(body: unknown, status: number): Response {
  return new Response(JSON.stringify(body), {
    status,
    headers: { ...corsHeaders, 'Content-Type': 'application/json' }
  });
}
